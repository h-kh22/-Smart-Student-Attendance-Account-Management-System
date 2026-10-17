package com.example.studentattendance;


import com.example.studentattendance.models.*;
import com.example.studentattendance.security.PasswordHasher;
import com.example.studentattendance.storage.CsvDataStore;
import org.apache.commons.csv.CSVRecord;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataModel {
    public static class AccountDataModel {
        private static ArrayList<Account> accounts = null;

        public AccountDataModel() {
            initialize();
        }

        public static synchronized void initialize() {
            if (accounts == null) {
                ArrayList<Account> loadedAccounts = new ArrayList<>();
                boolean migratedPasswords = false;
                if (CsvDataStore.fileExists("account.csv")) {
                    for (CSVRecord record : CsvDataStore.readRecords(
                            "account.csv", "code", "username", "password")) {
                        String password = record.get("password");
                        if (!PasswordHasher.isEncoded(password)) {
                            password = PasswordHasher.hash(password);
                            migratedPasswords = true;
                        }
                        String role = record.isMapped("role") ? record.get("role") : null;
                        int code = parseInt(record, "code", "account.csv");
                        String username = record.get("username");
                        loadedAccounts.add(new Account(code, username, password, role));
                    }
                }

                accounts = loadedAccounts;
                if (migratedPasswords) {
                    try {
                        persistAccounts();
                    } catch (UncheckedIOException e) {
                        accounts = null;
                        throw e;
                    }
                }
            }
        }

        public void saveAccounts() {
            persistAccounts();
        }

        private static void persistAccounts() {
            List<Object[]> records = new ArrayList<>();
            for (Account account : accounts) {
                records.add(new Object[]{account.getCode(), account.getUsername(), account.getPassword(), account.getRole()});
            }
            CsvDataStore.writeRecords("account.csv", List.of("code", "username", "password", "role"), records);
        }

        public boolean hasAccounts() {
            return !accounts.isEmpty();
        }

        public Account createInitialAdmin(String username, String password) {
            String normalizedUsername = validateUsername(username);
            validatePassword(password);
            if (!accounts.isEmpty()) {
                throw new IllegalStateException("An administrator account has already been created.");
            }

            Account admin = new Account(1, normalizedUsername, PasswordHasher.hash(password), "ADMIN");
            accounts.add(admin);
            try {
                saveAccounts();
            } catch (UncheckedIOException e) {
                accounts.remove(admin);
                throw e;
            }
            return admin;
        }

        public boolean authenticate(String username, String password) {
            Account account = getAccountByUsername(username);
            return account != null && PasswordHasher.verify(password, account.getPassword());
        }

        public void addAccount(Account account) {
            Objects.requireNonNull(account, "account");
            String username = validateUsername(account.getUsername());
            String password = account.getPassword();
            validatePassword(password);
            if (getAccountByUsername(username) != null) {
                throw new IllegalArgumentException("That username is already in use.");
            }

            account.setUsername(username);
            account.setPassword(PasswordHasher.hash(password));
            accounts.add(account);
            try {
                saveAccounts();
            } catch (UncheckedIOException e) {
                accounts.remove(account);
                throw e;
            }
        }

        public void updateAccount(Account account, String username, String newPassword) {
            Objects.requireNonNull(account, "account");
            if (!accounts.contains(account)) {
                throw new IllegalArgumentException("The selected account no longer exists.");
            }

            String normalizedUsername = validateUsername(username);
            Account existing = getAccountByUsername(normalizedUsername);
            if (existing != null && existing != account) {
                throw new IllegalArgumentException("That username is already in use.");
            }
            if (newPassword != null && !newPassword.isBlank()) {
                validatePassword(newPassword);
            }

            String previousUsername = account.getUsername();
            String previousPassword = account.getPassword();
            account.setUsername(normalizedUsername);
            if (newPassword != null && !newPassword.isBlank()) {
                account.setPassword(PasswordHasher.hash(newPassword));
            }
            try {
                saveAccounts();
            } catch (UncheckedIOException e) {
                account.setUsername(previousUsername);
                account.setPassword(previousPassword);
                throw e;
            }
        }

        private static String validateUsername(String username) {
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username is required.");
            }
            return username.trim();
        }

        private static void validatePassword(String password) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required.");
            }
        }

        public Account getAccountByUsername(String username) {
            for (Account account : accounts) {
                if (Objects.equals(account.getUsername(), username)) {
                    return account;
                }
            }
            return null;
        }

        public ArrayList<Account> getAccounts() {
            return accounts;
        }

        public Account deleteAccountByUsername(String username) {
            for (Account account : accounts) {
                if (Objects.equals(account.getUsername(), username)) {
                    accounts.remove(account);
                    saveAccounts();
                    return account;
                }
            }
            return null;
        }

        public boolean isAdmin(Account account) {
            return account != null && account.isAdmin();
        }

        public boolean isTeacher(Account account) {
            return account != null && account.isTeacher();
        }

        public boolean isStudent(Account account) {
            return account != null && account.isStudent();
        }

        public void updateAccountCode(Account account) {
            if (isAdmin(account)) {
                account.setCode(generateUniqueCode(1, 99));
            } else if (isTeacher(account)) {
                account.setCode(generateUniqueCode(100, 199));
            } else {
                account.setCode(generateUniqueCode(200, 99999));
            }
        }

        /** Generate next unique code based on role string. */
        public int generateNextCode(String role) {
            if (role == null) {
                // Default to student range
                return generateUniqueCode(200, 99999);
            }
            String r = role.trim().toUpperCase();
            if (r.contains("ADMIN")) {
                return generateUniqueCode(1, 99);
            } else if (r.contains("TEACH") || r.contains("DOCTOR") || r.contains("PROF")) {
                return generateUniqueCode(100, 199);
            } else {
                return generateUniqueCode(200, 99999);
            }
        }

        /** Verify plaintext password against stored hashed password. */
        public boolean verifyPassword(Account account, String plaintext) {
            if (account == null || plaintext == null) return false;
            return PasswordHasher.verify(plaintext, account.getPassword());
        }

        public int generateUniqueCode(int min, int max) {
            int uniqueCode;
            boolean unique;
            do {
                uniqueCode = (int) (Math.random() * (max - min + 1)) + min;
                unique = true;
                if (accounts != null) {
                    for (Account acc : accounts) {
                        if (acc.getCode() == uniqueCode) {
                            unique = false;
                            break;
                        }
                    }
                }
            } while (!unique);
            return uniqueCode;
        }

        public  String getUsernameByCode(int code) {
            for (Account account : accounts) {
                if (account.getCode() == code) {
                    return account.getUsername();
                }
            }
            return null;
        }

        public int getCodeByUsername(String username) {
            for (Account account : accounts) {
                if (account.getUsername().equals(username)) {
                    return account.getCode();
                }
            }
            return -1;
        }

        public void setUsernameByCode(int code, String newUsername) {
            for (Account account : accounts) {
                if (account.getCode() == code) {
                    account.setUsername(newUsername);
                    saveAccounts();
                    break;
                }
            }
        }

        public void removeAccount(Account account) {
            accounts.remove(account);
            saveAccounts();
        }
    }

    public static class LectureDataModel {
        private static ArrayList<Lecture> lectures = null;

        public LectureDataModel() {
            initialize();
        }

        public static synchronized void initialize() {
            if (lectures == null) {
                ArrayList<Lecture> loadedLectures = new ArrayList<>();
                if (CsvDataStore.fileExists("lecture.csv")) {
                    for (CSVRecord record : CsvDataStore.readRecords(
                            "lecture.csv", "lCode", "teacher_Code", "teacher_name", "name", "classroom")) {
                        loadedLectures.add(new Lecture(
                                parseInt(record, "lCode", "lecture.csv"),
                                parseInt(record, "teacher_Code", "lecture.csv"),
                                record.get("teacher_name"),
                                record.get("name"),
                                record.get("classroom")));
                    }
                } else {
                    loadedLectures.add(new Lecture(1, 120, "dfb", "java", "aa"));
                    loadedLectures.add(new Lecture(3, 150, "ad", "logic", "cc"));
                }
                lectures = loadedLectures;
            }
        }

        public void saveLecture() {
            List<Object[]> records = new ArrayList<>();
            for (Lecture lecture : lectures) {
                records.add(new Object[]{
                        lecture.getLCode(),
                        lecture.getTeacher_Code(),
                        lecture.getTeacher_name(),
                        lecture.getLName(),
                        lecture.getClassroom()
                });
            }
            CsvDataStore.writeRecords("lecture.csv",
                    List.of("lCode", "teacher_Code", "teacher_name", "name", "classroom"), records);
        }

        public void addLecture(Lecture lecture) {
            lectures.add(lecture);
            saveLecture();
        }

        public Lecture getLectureByLName(String name) {
            if (name == null) return null;
            for (Lecture lecture : lectures) {
                if (lecture.getLName() != null && lecture.getLName().equalsIgnoreCase(name)) {
                    return lecture;
                }
            }
            return null;
        }

        /** Alias for backward compatibility with tests. */
        public Lecture getLectureByName(String name) {
            return getLectureByLName(name);
        }

        public ArrayList<Lecture> getLectures() {
            return lectures;
        }

        public Lecture deleteLectureByName(String name) {
            for (Lecture lecture : lectures) {
                if (lecture.getLName() != null && lecture.getLName().equalsIgnoreCase(name)) {
                    lectures.remove(lecture);
                    saveLecture();
                    return lecture;
                }
            }
            return null;
        }

        public int getLCodeByLName(String lName) {
            for (Lecture lecture : lectures) {
                if (lecture.getLName() != null && lecture.getLName().equalsIgnoreCase(lName)) {
                    return lecture.getLCode();
                }
            }
            return -1;
        }

        public String getLNameByLCode(int code) {
            for (Lecture lecture : lectures) {
                if (lecture.getLCode() == code) {
                    return lecture.getLName();
                }
            }
            return null;
        }

        public String getTeacherNameByLName(String lName) {
            for (Lecture lecture : lectures) {
                if (lecture.getLName() != null && lecture.getLName().equalsIgnoreCase(lName)) {
                    return lecture.getTeacher_name();
                }
            }
            return null;
        }

        public void removeLecture(Lecture lecture) {
            lectures.remove(lecture);
            saveLecture();
        }
    }

    public static class StudentDataModel {
        private static ArrayList<Student> students = null;

        public StudentDataModel() {
            initialize();
        }
        public static synchronized void initialize() {
            if (students == null) {
                ArrayList<Student> loadedStudents = new ArrayList<>();
                if (CsvDataStore.fileExists("student.csv")) {
                    for (CSVRecord record : CsvDataStore.readRecords(
                            "student.csv", "id", "studentName", "mobileNumber", "residenceArea")) {
                        loadedStudents.add(new Student(
                                parseInt(record, "id", "student.csv"),
                                record.get("studentName"),
                                record.get("mobileNumber"),
                                record.get("residenceArea")));
                    }
                } else {
                    loadedStudents.add(new Student(1, "Student1", "05932", "adw"));
                    loadedStudents.add(new Student(2, "Student2", "05942", "awd"));
                }
                students = loadedStudents;
            }
        }

        public void saveStudent() {
            List<Object[]> records = new ArrayList<>();
            for (Student student : students) {
                records.add(new Object[]{
                        student.getID(),
                        student.getStudentName(),
                        student.getMobileNumber(),
                        student.getResidenceArea()
                });
            }
            CsvDataStore.writeRecords("student.csv",
                    List.of("id", "studentName", "mobileNumber", "residenceArea"), records);
        }

        public void addStudent(Student student) {
            students.add(student);
            saveStudent();
        }

        public Student getStudentByName(String name) {
            for (Student student : students) {
                if (student.getStudentName() != null && student.getStudentName().equalsIgnoreCase(name)) {
                    return student;
                }
            }
            return null;
        }

        /** Retrieve student by numeric ID. */
        public Student getStudentById(int id) {
            for (Student student : students) {
                if (student.getID() == id) {
                    return student;
                }
            }
            return null;
        }

        public ArrayList<Student> getStudents() {
            return students;
        }

        public Student deleteAccountByName(String name) {
            for (Student student : students) {
                if (student.getStudentName() != null && student.getStudentName().equalsIgnoreCase(name)) {
                    students.remove(student);
                    saveStudent();
                    return student;
                }
            }
            return null;
        }

        public  String getNameById(int id) {
            for (Student student : students) {
                if ( student.getID() == id) {
                    return student.getStudentName();
                }
            }
            return null;
        }

        public int getIdByName(String name) {
            for (Student student : students) {
                if (student.getStudentName() != null && student.getStudentName().equalsIgnoreCase(name)) {
                    return student.getID();
                }
            }
            return -1;
        }

       public void setNameById(int id, String name) {
            for (Student student : students) {
                if (student.getID() == id) {
                    student.setStudentName(name);
                    saveStudent();
                    break;
                }
           }
        }

        public void removeStudent(Student student) {
            students.remove(student);
            saveStudent();
        }

        public boolean isIDUnique(int id) {
            for (Student student : students) {
                if (student.getID() == id) {
                    return false; // ID already exists
                }
            }
            return true; // ID is unique
        }
        public int getNextUniqueID(int minId, int maxId) {
            int id = minId;
            boolean isUnique = false;

            while (!isUnique) {
                if (id > maxId) {
                    // Reset ID range if it exceeds the maximum
                    id = minId;
                }

                if (isIDUnique(id)) {
                    isUnique = true;
                } else {
                    id++;
                }
            }

            return id;
        }


    }

    public static class Teacher_StudentDataModel {

        private static ArrayList<Teacher_Student> teacher_students = null;

        public Teacher_StudentDataModel() {
            initialize();
        }

        public static synchronized void initialize() {
            if (teacher_students == null) {
                ArrayList<Teacher_Student> loadedTeacherStudents = new ArrayList<>();
                if (CsvDataStore.fileExists("teacher_Student.csv")) {
                    for (CSVRecord record : CsvDataStore.readRecords(
                            "teacher_Student.csv", "student_ID", "student_name", "teacher_Code",
                            "teacher_name", "l_Code", "l_Name", "attendance")) {
                        loadedTeacherStudents.add(new Teacher_Student(
                                parseInt(record, "student_ID", "teacher_Student.csv"),
                                record.get("student_name"),
                                parseInt(record, "teacher_Code", "teacher_Student.csv"),
                                record.get("teacher_name"),
                                parseInt(record, "l_Code", "teacher_Student.csv"),
                                record.get("l_Name"),
                                parseInt(record, "attendance", "teacher_Student.csv")));
                    }
                } else {
                    loadedTeacherStudents.add(new Teacher_Student(10002, "Student1", 208, "teacher1", 1, "", 25));
                    loadedTeacherStudents.add(new Teacher_Student(20000, "Student2", 210, "teacher2", 3, "", 50));
                }
                teacher_students = loadedTeacherStudents;
            }
        }

        public void saveTeacher_Student() {
            List<Object[]> records = new ArrayList<>();
            for (Teacher_Student teacherStudent : teacher_students) {
                records.add(new Object[]{
                        teacherStudent.getStudent_ID(),
                        teacherStudent.getStudent_name(),
                        teacherStudent.getTeacher_Code(),
                        teacherStudent.getTeacher_name(),
                        teacherStudent.getL_Code(),
                        teacherStudent.getL_Name(),
                        teacherStudent.getAttendance()
                });
            }
            CsvDataStore.writeRecords("teacher_Student.csv",
                    List.of("student_ID", "student_name", "teacher_Code", "teacher_name",
                            "l_Code", "l_Name", "attendance"),
                    records);
        }

        public void addTeacher_Student(Teacher_Student teacher_student) {
            teacher_students.add(teacher_student);
            saveTeacher_Student();
        }



        public ArrayList<Teacher_Student> getTeacher_students() {
            return teacher_students;
        }

        public ArrayList<Teacher_Student> getTeacher_Student() {
            return teacher_students;
        }

        public void removeTeacher_Student(Teacher_Student teacher_student) {
            teacher_students.remove(teacher_student);
            saveTeacher_Student();
        }

        public void updateTeacher_Student(Teacher_Student updatedTeacherStudent) {
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == updatedTeacherStudent.getStudent_ID()) {
                    teacherStudent.setAttendance(updatedTeacherStudent.getAttendance());
                    break;
                }
            }
            saveTeacher_Student();
        }

        public int getAttendanceByIdAndLecture(int studentID, String lectureName) {
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == studentID
                        && (lectureName == null || (teacherStudent.getL_Name() != null && teacherStudent.getL_Name().equalsIgnoreCase(lectureName)))) {
                    return teacherStudent.getAttendance();
                }
            }
            return -1; // Return -1 if the attendance is not found
        }

        public void updateAttendanceByIdAndLecture(int studentID, String lectureName, int newAttendance) {
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == studentID
                        && (lectureName == null || (teacherStudent.getL_Name() != null && teacherStudent.getL_Name().equalsIgnoreCase(lectureName)))) {
                    teacherStudent.setAttendance(newAttendance);
                    saveTeacher_Student();
                    break;
                }
            }
        }

        public void checkAndUpdateAttendance() {
            // Safe no-op check to avoid arbitrary student deletions or ConcurrentModificationExceptions
        }

        public ArrayList<Teacher_Student> getTeacher_StudentsByLectureName(String lectureName) {
            ArrayList<Teacher_Student> matchingStudents = new ArrayList<>();
            for (Teacher_Student teacherStudent : teacher_students) {
                if (lectureName == null || (teacherStudent.getL_Name() != null && teacherStudent.getL_Name().equalsIgnoreCase(lectureName))) {
                    matchingStudents.add(teacherStudent);
                }
            }
            return matchingStudents;
        }

        public ArrayList<Teacher_Student> getTeacher_StudentsByTeacherName(String teacherName) {
            ArrayList<Teacher_Student> matchingStudents = new ArrayList<>();
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getTeacher_name() != null && teacherStudent.getTeacher_name().equalsIgnoreCase(teacherName)) {
                    matchingStudents.add(teacherStudent);
                }
            }
            return matchingStudents;
        }

        public ArrayList<Teacher_Student> getTeacher_StudentsByStudentID(int studentID) {
            ArrayList<Teacher_Student> matchingStudents = new ArrayList<>();
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == studentID) {
                    matchingStudents.add(teacherStudent);
                }
            }
            return matchingStudents;
        }

        public ArrayList<Teacher_Student> getTeacher_StudentsByStudentName(String studentName) {
            ArrayList<Teacher_Student> matchingStudents = new ArrayList<>();
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_name() != null && teacherStudent.getStudent_name().equalsIgnoreCase(studentName)) {
                    matchingStudents.add(teacherStudent);
                }
            }
            return matchingStudents;
        }

        public void takeAttendance(int studentID, String lectureName) {
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == studentID
                        && (lectureName == null || (teacherStudent.getL_Name() != null && lectureName.equalsIgnoreCase(teacherStudent.getL_Name())))) {
                    int currentAttendance = teacherStudent.getAttendance();
                    teacherStudent.setAttendance(currentAttendance + 1);
                    break;
                }
            }
        }

        public void takeAttendance(int studentID) {
            takeAttendance(studentID, null);
        }

        public Teacher_Student getTeacher_StudentByName(String name) {
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_name() != null && teacherStudent.getStudent_name().equalsIgnoreCase(name)) {
                    return teacherStudent;
                }
            }
            return null; // Return null if the student with the given name is not found
        }

        public Teacher_Student getTeacher_StudentById(int studentID) {
            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == studentID) {
                    return teacherStudent;
                }
            }
            return null; // Return null if the student with the given ID is not found
        }
        public ArrayList<Teacher_Student> getLectureAndAttendanceDetails(Teacher_Student student) {
            ArrayList<Teacher_Student> studentDetails = new ArrayList<>();

            for (Teacher_Student teacherStudent : teacher_students) {
                if (teacherStudent.getStudent_ID() == student.getStudent_ID()) {
                    studentDetails.add(teacherStudent);
                }
            }

            return studentDetails;
        }
    }

    private static int parseInt(CSVRecord record, String column, String fileName) {
        try {
            return Integer.parseInt(record.get(column).trim());
        } catch (NumberFormatException e) {
            // Log warning and return a sentinel value indicating invalid data
            System.getLogger(DataModel.class.getName()).log(System.Logger.Level.WARNING,
                    "Invalid integer in " + fileName + " at record " + record.getRecordNumber()
                            + ", column " + column + ". Defaulting to -1.");
            return -1;
        }
    }
}
