package com.example.studentattendance;

import com.example.studentattendance.models.Account;
import com.example.studentattendance.models.Lecture;
import com.example.studentattendance.models.Student;
import com.example.studentattendance.models.Teacher_Student;
import com.example.studentattendance.security.PasswordHasher;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SystemAuditAndRBATest {

    private static String setupStatus = "NOT_STARTED";
    private static Throwable setupThrowable = null;

    @BeforeAll
    public static void setup() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                latch.countDown();
            });
            boolean ok = latch.await(5, TimeUnit.SECONDS);
            setupStatus = ok ? "INITIALIZED_OK" : "LATCH_TIMEOUT";
        } catch (Throwable t) {
            setupStatus = "EXCEPTION: " + t.getClass().getName() + " - " + t.getMessage();
            setupThrowable = t;
        }
        LanguageManager.loadPreferences();
    }

    @Test
    @DisplayName("Test 1: RBAC Roles and Code Ranges for Admin, Doctor/Teacher, and Student")
    public void testRoleRBACRanges() {
        DataModel.AccountDataModel accountModel = new DataModel.AccountDataModel();

        // 1. Admin code range: 1 to 99
        int adminCode = accountModel.generateNextCode("ADMIN");
        assertTrue(adminCode >= 1 && adminCode < 100, "Admin code must be within [1, 99]. Got: " + adminCode);
        Account adminAcc = new Account(adminCode, "testAdmin", "secret", "ADMIN");
        assertTrue(adminAcc.isAdmin(), "Account should be ADMIN");
        assertFalse(adminAcc.isTeacher(), "Admin should not be TEACHER");
        assertFalse(adminAcc.isStudent(), "Admin should not be STUDENT");

        // 2. Teacher/Doctor code range: 100 to 199
        int teacherCode = accountModel.generateNextCode("TEACHER");
        assertTrue(teacherCode >= 100 && teacherCode < 200, "Doctor/Teacher code must be within [100, 199]. Got: " + teacherCode);
        Account teacherAcc = new Account(teacherCode, "testDoctor", "secret", "TEACHER");
        assertTrue(teacherAcc.isTeacher(), "Account should be TEACHER");
        assertFalse(teacherAcc.isAdmin(), "Teacher should not be ADMIN");
        assertFalse(teacherAcc.isStudent(), "Teacher should not be STUDENT");

        // 3. Student default code range: 200 to 99999
        int studentCode = accountModel.generateNextCode("STUDENT");
        assertTrue(studentCode >= 200 && studentCode < 100000, "Student code must be within [200, 99999]. Got: " + studentCode);
        Account studentAcc = new Account(studentCode, "testStudent", "secret", "STUDENT");
        assertTrue(studentAcc.isStudent(), "Account should be STUDENT");
        assertFalse(studentAcc.isAdmin(), "Student should not be ADMIN");
        assertFalse(studentAcc.isTeacher(), "Student should not be TEACHER");

        // 4. Default fallback when role is null or unknown
        int defaultCode = accountModel.generateNextCode(null);
        assertTrue(defaultCode >= 200, "Default role without selection must be STUDENT (code >= 200). Got: " + defaultCode);
    }

    @Test
    @DisplayName("Test 2: Global i18n ResourceBundle Parity and RTL Orientation")
    public void testI18nAndRTL() throws Exception {
        // Test switching to Arabic
        LanguageManager.setLanguage("ar");
        assertTrue(LanguageManager.isArabic(), "LanguageManager must report isArabic == true");
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, LanguageManager.getNodeOrientation(), "Arabic must set RIGHT_TO_LEFT");

        String arAdminTitle = LanguageManager.get("admin.header.title");
        assertNotNull(arAdminTitle);
        assertFalse(arAdminTitle.startsWith("!"), "Key admin.header.title must exist in Arabic");

        // Test switching to English
        LanguageManager.setLanguage("en");
        assertFalse(LanguageManager.isArabic(), "LanguageManager must report isArabic == false");
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, LanguageManager.getNodeOrientation(), "English must set LEFT_TO_RIGHT");

        String enAdminTitle = LanguageManager.get("admin.header.title");
        assertEquals("Admin Dashboard", enAdminTitle);

        // Verify property key parity between English and Arabic
        Properties enProps = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/com/example/studentattendance/i18n/messages_en.properties")) {
            assertNotNull(is, "messages_en.properties must be in classpath");
            enProps.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        }

        Properties arProps = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/com/example/studentattendance/i18n/messages_ar.properties")) {
            assertNotNull(is, "messages_ar.properties must be in classpath");
            arProps.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        }

        for (String key : enProps.stringPropertyNames()) {
            assertTrue(arProps.containsKey(key), "Arabic properties is missing key: " + key);
        }
    }

    @Test
    @DisplayName("Test 3: Attendance Recording and Persistence Null-Safety")
    public void testAttendanceLogic() {
        assertEquals("INITIALIZED_OK", setupStatus, () -> "Setup failed with: " + setupThrowable);
        DataModel.Teacher_StudentDataModel tsModel = new DataModel.Teacher_StudentDataModel();
        DataModel.LectureDataModel lectureModel = new DataModel.LectureDataModel();
        DataModel.StudentDataModel studentModel = new DataModel.StudentDataModel();

        // Safe null handling
        assertNull(lectureModel.getLectureByName(null));
        assertNull(lectureModel.getLectureByName("NonExistentCourseXYZ"));
        assertNull(studentModel.getStudentByName(null));
        assertNull(studentModel.getStudentById(-999));
        assertNull(tsModel.getTeacher_StudentById(-999));

        // Create test lecture
        Lecture testLecture = new Lecture(9999, 150, "Dr. Test", "Software Engineering", "Lab 101");
        lectureModel.addLecture(testLecture);
        assertEquals("Dr. Test", lectureModel.getTeacherNameByLName("Software Engineering"));

        // Create test student
        Student testStudent = new Student(88888, "Auditor Student", "0599000000", "Campus Center");
        studentModel.addStudent(testStudent);
        assertNotNull(studentModel.getStudentById(88888));

        // Create relationship
        Teacher_Student ts = new Teacher_Student(88888, "Auditor Student", 150, "Dr. Test", 9999, "Software Engineering", 5);
        tsModel.addTeacher_Student(ts);

        int initialAtt = tsModel.getAttendanceByIdAndLecture(88888, "Software Engineering");
        assertEquals(5, initialAtt);

        // Take attendance (increments by 1)
        tsModel.takeAttendance(88888, "Software Engineering");
        int updatedAtt = tsModel.getAttendanceByIdAndLecture(88888, "Software Engineering");
        assertEquals(6, updatedAtt, "Taking attendance should increment count to 6");

        // Cleanup test entries to avoid polluting data
        tsModel.removeTeacher_Student(ts);
        lectureModel.removeLecture(testLecture);
        studentModel.removeStudent(testStudent);
    }

    @Test
    @DisplayName("Test 4: Account Data Model Password Hashing and Verification")
    public void testPasswordSecurity() {
        DataModel.AccountDataModel accountModel = new DataModel.AccountDataModel();

        // Plaintext passwords and PBKDF2 verification
        String plainPassword = "mypassword123";
        String hashedPassword = PasswordHasher.hash(plainPassword);
        assertTrue(PasswordHasher.isEncoded(hashedPassword), "Password must be PBKDF2-encoded");

        Account temp = new Account(999, "tempUser", hashedPassword, "STUDENT");
        assertTrue(accountModel.verifyPassword(temp, plainPassword), "Hashed password should verify against plaintext");
        assertFalse(accountModel.verifyPassword(temp, "wrongpassword"), "Wrong password must fail verification");
        assertFalse(accountModel.verifyPassword(temp, null), "Null password must fail verification");
        assertFalse(accountModel.verifyPassword(null, plainPassword), "Null account must fail verification");
    }
}
