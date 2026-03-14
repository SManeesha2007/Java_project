package com.resultsystem.management;

import java.util.Scanner;

import com.resultsystem.database.DBConnection;
import com.resultsystem.service.ResultService;
import com.resultsystem.service.ResultServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResultManagement {

    static Scanner sc = new Scanner(System.in);

    static ResultService service = new ResultServiceImpl();

    public static void menu(){

        while(true){

            System.out.println("\n--- RESULT MANAGEMENT ---");
            System.out.println("1 Add Result");
            System.out.println("2 View Result");
            System.out.println("3 Update Result");
            System.out.println("4 Back");
            System.out.print("Enter your choice: ");

            int ch = sc.nextInt();
            sc.nextLine();

            switch(ch){

                case 1 -> service.addResult();
                case 2 -> service.viewResults();
                case 3 -> service.updateResult();
                case 4 -> { return; }

                default -> System.out.println("Invalid choice!");
            }
        }
    }

    public static void generateTranscript(String roll) {

        try {

            Connection con = DBConnection.getConnection();

            Document doc = new Document();

            PdfWriter.getInstance(doc, new FileOutputStream("Transcript_" + roll + ".pdf"));

            doc.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font textFont = new Font(Font.FontFamily.HELVETICA, 11);

            Paragraph title = new Paragraph("STUDENT RESULT TRANSCRIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            doc.add(new Paragraph("------------------------------------------------------------"));

            // ---------------- STUDENT DETAILS ----------------

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM students WHERE roll=?");

            ps.setString(1, roll);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                doc.add(new Paragraph("Name              : " + rs.getString("name"), textFont));
                doc.add(new Paragraph("Roll Number       : " + rs.getString("roll"), textFont));
                doc.add(new Paragraph("Department        : " + rs.getString("department"), textFont));
                doc.add(new Paragraph("Current Semester  : " + rs.getInt("semester"), textFont));
            }

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("============================================================"));

            // ---------------- RESULT SECTION ----------------

            PreparedStatement pst = con.prepareStatement(
                    "SELECT semester,subject,marks,grade FROM results WHERE roll=? ORDER BY semester");

            pst.setString(1, roll);

            ResultSet r = pst.executeQuery();

            int prevSem = -1;
            PdfPTable table = null;

            double totalCGPA = 0;
            int semCount = 0;

            int subjectCount = 0;
            double gradePoints = 0;

            while(r.next()){

                int sem = r.getInt("semester");

                if(sem != prevSem){

                    if(table != null){

                        doc.add(table);

                        double sgpa = gradePoints / subjectCount;

                        doc.add(new Paragraph("SGPA : " + String.format("%.2f", sgpa), headerFont));
                        doc.add(new Paragraph("------------------------------------------------------------"));

                        totalCGPA += sgpa;
                        semCount++;

                        subjectCount = 0;
                        gradePoints = 0;
                    }

                    doc.add(new Paragraph(" "));
                    doc.add(new Paragraph("SEMESTER " + sem, headerFont));

                    table = new PdfPTable(3);
                    table.setWidthPercentage(100);

                    table.addCell("Subject");
                    table.addCell("Marks");
                    table.addCell("Grade");

                    prevSem = sem;
                }

                String subject = r.getString("subject");
                int marks = r.getInt("marks");
                String grade = r.getString("grade");

                table.addCell(subject);
                table.addCell(String.valueOf(marks));
                table.addCell(grade);

                subjectCount++;

                gradePoints += getGradePoint(grade);
            }

            if(table != null){

                doc.add(table);

                double sgpa = gradePoints / subjectCount;

                doc.add(new Paragraph("SGPA : " + String.format("%.2f", sgpa), headerFont));

                totalCGPA += sgpa;
                semCount++;
            }

            double cgpa = totalCGPA / semCount;

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("FINAL CGPA : " + String.format("%.2f", cgpa), headerFont));

            doc.add(new Paragraph("============================================================"));

            // ---------------- INTERNAL MARKS ----------------

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("INTERNAL MARKS", headerFont));

            PdfPTable internalTable = new PdfPTable(4);
            internalTable.setWidthPercentage(100);

            internalTable.addCell("Subject");
            internalTable.addCell("Mid 1");
            internalTable.addCell("Mid 2");
            internalTable.addCell("Total");

            PreparedStatement ips = con.prepareStatement(
                    "SELECT subject,mid1,mid2 FROM internal_marks WHERE roll=?");

            ips.setString(1, roll);

            ResultSet irs = ips.executeQuery();

            while(irs.next()){

                int mid1 = irs.getInt("mid1");
                int mid2 = irs.getInt("mid2");

                int total = mid1 + mid2;

                internalTable.addCell(irs.getString("subject"));
                internalTable.addCell(String.valueOf(mid1));
                internalTable.addCell(String.valueOf(mid2));
                internalTable.addCell(String.valueOf(total));
            }

            doc.add(internalTable);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Generated by Student Result Management System"));

            doc.close();

            System.out.println("Transcript generated successfully!");
            System.out.println("File: Transcript_" + roll + ".pdf");

        }
        catch(Exception e){

            System.out.println("Error generating transcript: " + e.getMessage());
        }
    }
    private static double getGradePoint(String grade){

        switch(grade){

            case "A+" : return 10;
            case "A"  : return 9;
            case "B+" : return 8;
            case "B"  : return 7;
            case "C"  : return 6;
            case "D"  : return 5;
            default   : return 0;
        }
    }
}