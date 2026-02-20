package com.courseplanner;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PlanExporter {

    public static void exportToTextFile(List<List<Course>> plan, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        writer.write("=".repeat(60));
        writer.newLine();
        writer.write("ACADEMIC STUDY PLAN");
        writer.newLine();
        writer.write("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        writer.newLine();
        writer.write("=".repeat(60));
        writer.newLine();
        writer.newLine();

        int totalCredits = 0;
        int totalSemesters = plan.size();

        for (int i = 0; i < plan.size(); i++) {
            List<Course> semester = plan.get(i);
            int semesterCredits = calculateSemesterCredits(semester);
            totalCredits += semesterCredits;

            writer.write(String.format("SEMESTER %d (%d credits)", i + 1, semesterCredits));
            writer.newLine();
            writer.write("-".repeat(40));
            writer.newLine();

            for (Course course : semester) {
                writer.write(String.format("• %s: %s", course.getCode(), course.getName()));
                writer.newLine();
                writer.write(String.format("  Credits: %d | Status: %s",
                        course.getCredits(),
                        course.isCompleted() ? "Completed" : "Pending"));
                writer.newLine();
            }
            writer.newLine();
        }

        writer.write("=".repeat(60));
        writer.newLine();
        writer.write(String.format("SUMMARY: %d semesters, %d total credits", totalSemesters, totalCredits));
        writer.newLine();
        writer.write("=".repeat(60));

        writer.close();
    }

    public static void exportToCSV(List<List<Course>> plan, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        writer.write("Semester,Course Code,Course Name,Credits,Status,Grade");
        writer.newLine();

        for (int i = 0; i < plan.size(); i++) {
            List<Course> semester = plan.get(i);
            for (Course course : semester) {
                writer.write(String.format("%d,%s,\"%s\",%d,%s,%.1f",
                        i + 1,
                        course.getCode(),
                        course.getName(),
                        course.getCredits(),
                        course.isCompleted() ? "Completed" : "Planned",
                        course.getGrade()
                ));
                writer.newLine();
            }
        }

        writer.close();
    }

    public static void exportToICS(List<List<Course>> plan, String filename, int startYear) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        writer.write("BEGIN:VCALENDAR");
        writer.newLine();
        writer.write("VERSION:2.0");
        writer.newLine();
        writer.write("PRODID:-//Course Planner//EN");
        writer.newLine();
        writer.write("CALSCALE:GREGORIAN");
        writer.newLine();
        writer.write("METHOD:PUBLISH");
        writer.newLine();

        int eventId = 1;

        String[] semesterStartDates = {
                String.format("%d0901", startYear),
                String.format("%d0115", startYear + 1),
                String.format("%d0901", startYear + 1),
                String.format("%d0115", startYear + 2),
                String.format("%d0901", startYear + 2),
                String.format("%d0115", startYear + 3)
        };

        for (int i = 0; i < plan.size(); i++) {
            if (i < semesterStartDates.length) {
                List<Course> semester = plan.get(i);
                String semesterStart = semesterStartDates[i];

                for (Course course : semester) {
                    writer.write("BEGIN:VEVENT");
                    writer.newLine();
                    writer.write("UID:course-" + eventId + "@courseplanner");
                    writer.newLine();
                    writer.write("DTSTAMP:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "T000000Z");
                    writer.newLine();
                    writer.write("DTSTART;VALUE=DATE:" + semesterStart);
                    writer.newLine();
                    writer.write("DTEND;VALUE=DATE:" + semesterStart);
                    writer.newLine();
                    writer.write("SUMMARY:" + course.getCode() + " - " + course.getName());
                    writer.newLine();
                    writer.write("DESCRIPTION:Start of " + course.getName() + "\\nCredits: " + course.getCredits());
                    writer.newLine();
                    writer.write("LOCATION:University Campus");
                    writer.newLine();
                    writer.write("END:VEVENT");
                    writer.newLine();

                    eventId++;
                }
            }
        }

        writer.write("END:VCALENDAR");
        writer.close();
    }

    public static void saveProgress(List<String> completedCourses,
                                    Map<String, Double> grades,
                                    List<String> interests,
                                    String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        writer.write("# Student Progress - Course Planner");
        writer.newLine();
        writer.write("# Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.newLine();
        writer.newLine();

        writer.write("[COMPLETED_COURSES]");
        writer.newLine();
        for (String course : completedCourses) {
            writer.write(course);
            writer.newLine();
        }
        writer.newLine();

        writer.write("[GRADES]");
        writer.newLine();
        for (Map.Entry<String, Double> entry : grades.entrySet()) {
            writer.write(entry.getKey() + "=" + entry.getValue());
            writer.newLine();
        }
        writer.newLine();

        writer.write("[INTERESTS]");
        writer.newLine();
        for (String interest : interests) {
            writer.write(interest);
            writer.newLine();
        }

        writer.close();
    }

    public static void loadProgress(String filename,
                                    List<String> completedCourses,
                                    Map<String, Double> grades,
                                    List<String> interests) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        String section = "";

        completedCourses.clear();
        grades.clear();
        interests.clear();

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.equals("[COMPLETED_COURSES]")) {
                section = "courses";
                continue;
            } else if (line.equals("[GRADES]")) {
                section = "grades";
                continue;
            } else if (line.equals("[INTERESTS]")) {
                section = "interests";
                continue;
            }

            switch (section) {
                case "courses":
                    completedCourses.add(line);
                    break;
                case "grades":
                    String[] gradeParts = line.split("=");
                    if (gradeParts.length == 2) {
                        grades.put(gradeParts[0], Double.parseDouble(gradeParts[1]));
                    }
                    break;
                case "interests":
                    interests.add(line);
                    break;
            }
        }

        reader.close();
    }

    private static int calculateSemesterCredits(List<Course> semester) {
        return semester.stream()
                .mapToInt(Course::getCredits)
                .sum();
    }
}