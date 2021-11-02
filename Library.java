package com.company1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


public class Library {
    private static Connection con = null;
    private static Scanner in = new Scanner(System.in);

    public static final String reset="\u001B[0m";
    public static final String red="\u001B[31m";
    public static final String green="\u001B[32m";
    public static final String cyan="\u001B[36m";

    util u=new util();
    public static void main(String[] args) throws SQLException
    {
        //create database first and change in url name of database
        Library ref = new Library();
        int ch,flag=0;
        String t;
         try {
        String url = "jdbc:mysql://localhost:3306/constructlib";
        String user = "root";
        String pass = "312121Zz@";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception c) {
            return;
        }
        con = DriverManager.getConnection(url, user, pass);

        while (true) {

            if(flag==0){
            System.out.print(cyan+"1.add new book to library  ");
            System.out.print("2.view all books  ");
            System.out.print("3.borrow a book  ");
            System.out.print("4.return a book  ");
            System.out.print("5.remove a book from library  ");
            System.out.println("6.exit"+reset);}
            flag=0;
            t=in.nextLine();
            if(t.compareTo("")==0){
                flag=1;
                continue;
            }
            try{
            ch = Integer.parseInt(t); }
            catch(Exception c){
                System.out.println(red+"please enter one of above options"+reset);
                continue;
            }

            if (ch == 1) {
                ref.insertrecord();
            } else if (ch == 2) {
                ref.viewallbooks();
                System.out.println();
            } else if (ch == 3) {
                ref.borrow();
            } else if (ch == 4) {
                ref.returnbook();
            } else if (ch == 5) {
                ref.removebook();
            }
            else if (ch == 6) {
                con.close();
                break;
            }
            else{
                System.out.println(red+"please enter only one of above options"+reset);
            }
        }

        } catch (Exception c) {
            throw new RuntimeException("something went wrong");
        }
    }

    private void insertrecord() throws SQLException
    {
        String sql = "insert into book(name,location) values(?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        System.out.println(green+"Enter name of book:");
        String nm=in.nextLine();
        ps.setString(1,nm);
        System.out.println("Enter location in library:");
        ps.setString(2, in.nextLine());
        ps.executeUpdate();
            System.out.println( "Successfully added into library database"+reset);
    }

    private void viewallbooks() throws SQLException
    {
        String sql = "select * from book";
        Statement st = con.createStatement();
        String nm, loc, sta;
        int id;
        ResultSet res = st.executeQuery(sql);
        int i = 0;
        while (res.next()) {
            i++;
            nm = res.getString("name");
            loc = res.getString("location");
            sta = res.getString("status");
            id = res.getInt("bid");
            System.out.println(green+"Bookid=" + id + "  Bookname:" + nm + "  Location:" + loc +
                    " status:" + sta+reset);
        }
        if (i == 0)
            System.out.println(red+"No books in stock..."+reset);
    }

    private void borrow() throws SQLException
    {
        System.out.println(green+"Enter id of book you want to borrow:"+reset);
        int bid;
        try{
        bid = Integer.parseInt(in.nextLine()); }
        catch(Exception c){
            System.out.println(red+"please enter a valid book id"+reset);
            return ;
        }
        String sta;
        String sql = "select * from book where bid = " + bid;
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet res = pst.executeQuery(sql);
        ResultSet res1 = res;
        if (res.next() == false) {
            System.out.println(red+"book is not there in stock"+reset);
            return;
        }
        sta = res.getString("status");
        if (sta.compareTo("Not Available") == 0) {
            int sid=res1.getInt("sid");
            sql = "select * from student where sid = " + sid;
            pst = con.prepareStatement(sql);
            res = pst.executeQuery(sql);
            res.next();
            String snm,stdate,retdate,phno;
              snm=res.getString("name");
              stdate=res.getString("stdate");
              retdate=res.getString("returndate");
              phno=res.getString("phno");
            System.out.println(red+"book is already borrowed by someone"+reset);
            System.out.println(green+"Expected date to be available:"+retdate+reset);
            System.out.println(green+"want to see borrowed student details(y/n)?"+reset);
            char c=in.nextLine().charAt(0);
            if(c=='y'){
                System.out.println(green+"Name:"+snm+" borrowed on:"+stdate+
                        " contact number:"+phno);
                System.out.println("Expected date to be returned by him:" + retdate+reset);
            }
            else if(c=='n'){
            }
            else{
                System.out.println(red+"invalid input"+reset);
            }
            System.out.println(red+"Borrow operation unsuccessful"+reset);
        }

        else {
            System.out.println(green+"You must enter your details to borrow the book");
            String sql1 = "insert into student(name,stdate,returndate,phno) values(?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql1);
            String st,ret;
            System.out.println("Enter your name:");
            ps.setString(1, in.nextLine());
            SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy");
            st = ft.format(new Date());
            ps.setString(2,st);
            System.out.println("Enter your expected return date in (dd/mm/yyyy):");
            System.out.println("(you are only allowed to put a return date within 30 days" +
                    " from today");
            ret=in.nextLine();
            //check return date
            if(u.isValidDate(ret)==false){
                System.out.println(red+"please enter a valid date in specified format"+reset);
                System.out.println(red+"Borrow operation unsuccessful"+reset);
                return ;
            }
            int days=u.getDifference(st,ret);
            if(days<0) {
                System.out.println(red + "dont enter a date which is " +
                        "already completed for borrowing the book" + reset);
                System.out.println(red+"Borrow operation unsuccessful"+reset);
                return ;
            }
            if(days>30){
                System.out.println(red+"you are not allowed to borrow the book");
                System.out.println("your borrow period should not be more than 30 days");
                System.out.println("Borrow operation unsuccessful"+reset);
                return ;
            }
            ps.setString(3,ret);
            System.out.println("Enter your contact number:");
            String ph=in.nextLine();
            ps.setString(4,ph);
            //check validity of phone number
            if(u.isvalidphno(ph)==false){
                System.out.println(red+"please enter a valid contact number " +
                        "excluding country code"+reset);
                System.out.println(red+"Borrow operation unsuccessful"+reset);
                return ;
            }
            //modifying database starts after all checkings and validation
            //1)inserting student tuple into database
            int rows = ps.executeUpdate();
            if (rows > 0)
                System.out.println("inserted successful"+reset);
            else{
                System.out.println(red+"wrong"+reset);
                return ;}
            //2)changing status of book
            int id = res.getInt("bid");
            sql = "update book set status='Not Available' where bid =" + id;
            ps = con.prepareStatement(sql);
            ps.executeUpdate();
            //3)assigning sid of newly inserted student to book borrowed sid attribute
            sql = "select max(sid) from student";
            ps = con.prepareStatement(sql);
            res = ps.executeQuery();
            res.next();
            int sid = res.getInt(1);
            sql = "update book set sid = " + sid + " where bid = " + id;
            ps = con.prepareStatement(sql);
            ps.executeUpdate();
            System.out.println(green+"Successfully borrowed"+reset);
        }
    }

    private void returnbook() throws SQLException
    {
        System.out.println(green+"Enter book id you want to return:"+reset);
        int bid;
        try{
        bid = Integer.parseInt(in.nextLine());}
        catch(Exception c){
            System.out.println(red+"please enter a valid book id"+reset);
            return ;
        }
        String s;
        String sql = "select * from book where bid = " + bid;
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet res = pst.executeQuery(sql);
        if (res.next()) {
            s = res.getString("status");
            if (s.compareTo("Available") == 0) {
                System.out.println(red+"Book is already returned to library"+reset);
            }
            else {
                SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy");
               String t = ft.format(new Date());
                sql="select * from book where bid = "+bid;
                 pst = con.prepareStatement(sql);
                res = pst.executeQuery(sql);
                res.next();
                   int sid = res.getInt("sid");
                //get returndate of borrower
                 sql = "select * from student where sid = " + sid;
        pst = con.prepareStatement(sql);
        res = pst.executeQuery(sql);
        res.next();
                    String ret=res.getString("returndate");
                int days=u.getDifference(ret,t);

                //imposing fine
                if(days>0){
                    System.out.println(red+"you have a fine of "+days+"rs"+reset);
                }
                //modifying database starts after all validations
                //1)change status of book
                sql = "update book set status = 'Available' where bid = " + bid;
                pst = con.prepareStatement(sql);
                pst.executeUpdate();
                //2)set sid=null for given book
                sql = "update book set sid=null where bid = " + bid;
                pst = con.prepareStatement(sql);
                pst.executeUpdate();
                //3)delete borrower tuple in db
                sql = "delete from student where sid = " + sid;
                pst = con.prepareStatement(sql);
                pst.executeUpdate();
                System.out.println(green+"Successfully returned to library"+reset);
            }

        } else {
            System.out.println(red+"Book is not present in library records"+reset);
        }
    }

    private void removebook() throws SQLException
    {
        System.out.println(green+"Enter book id you want to remove:"+reset);
        int bid;
        try{
         bid = Integer.parseInt(in.nextLine());}
        catch(Exception c){
            System.out.println(red+"please enter a valid book id"+reset);
            return ;
        }
        String s;
        String sql = "select * from book where bid = " + bid;
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet res = pst.executeQuery(sql);
        if (res.next()) {
            s = res.getString("status");
            if (s.compareTo("Not Available") == 0) {
                int sid = res.getInt("sid");
                sql = "select * from student where sid = " + sid;
                pst = con.prepareStatement(sql);
                res = pst.executeQuery(sql);
                res.next();
                String snm, stdate, retdate, phno;
                snm = res.getString("name");
                stdate = res.getString("stdate");
                retdate = res.getString("returndate");
                phno = res.getString("phno");
                System.out.println(red+"This book is currently borrowed by one student"+reset);
                System.out.println(red+"This book can be removed only after the student" +
                        " returns the book"+reset);
                System.out.println(red+"Expected date to be available:" + retdate+reset);
                System.out.println(green+"Do you want to see borrowed student details(y/n)?"+reset);
                char c = in.nextLine().charAt(0);
                if (c == 'y') {
                    System.out.println(green+"Name:" + snm + " Borrowed on:" + stdate +
                            " Contact number:" + phno);
                    System.out.println("Expected date to be returned by him:" + retdate+reset);
                } else if (c == 'n') {

                } else {
                    System.out.println(red+"invalid input"+reset);
                }
                System.out.println(red+"remove operation unsuccessful"+reset);
            }
            else {
                sql = "delete from book where bid = " + bid;
                pst = con.prepareStatement(sql);
                pst.executeUpdate();
                System.out.println(green+"Succesfully removed book with id="+bid+
                        " from library records"+reset);
            }
        } else {
            System.out.println(red+"Book is not there in library records"+reset);
        }
    }

}


class util{
    int MAX_VALID_YR = 9999;
    int MIN_VALID_YR = 1800;
    int monthDays[] = {31, 28, 31, 30, 31, 30,
            31, 31, 30, 31, 30, 31};

    public boolean isLeap(int year)
    {
        return (((year % 4 == 0) &&
                (year % 100 != 0)) ||
                (year % 400 == 0));
    }

    public boolean isValidDate(String s)
    {
        //(dd/mm/yyyy)
        int d,m,y;
        try{
            d=Integer.parseInt(s.substring(0,2));
            m=Integer.parseInt(s.substring(3,5));
            y=Integer.parseInt(s.substring(6,10)); }
        catch(Exception c){
            return false;
        }
        if (y > MAX_VALID_YR ||
                y < MIN_VALID_YR)
            return false;
        if (m < 1 || m > 12)
            return false;
        if (d < 1 || d > 31)
            return false;
        if (m == 2)
        {
            if (isLeap(y))
                return (d <= 29);
            else
                return (d <= 28);
        }
        if (m == 4 || m == 6 ||
                m == 9 || m == 11)
            return (d <= 30);
        return true;
    }

    public int countLeapYears(int d,int m,int y)
    {
        int years = y;
        if (m<= 2)
        {
            years--;
        }

        return years / 4 - years / 100 + years / 400;
    }

    public int getDifference(String st,String ret)
    {
        int ds,ms,ys;
        int dr,mr,yr;
        ds=Integer.parseInt(st.substring(0,2));
        ms=Integer.parseInt(st.substring(3,5));
        ys=Integer.parseInt(st.substring(6,10));
        dr=Integer.parseInt(ret.substring(0,2));
        mr=Integer.parseInt(ret.substring(3,5));
        yr=Integer.parseInt(ret.substring(6,10));

     
        int n1 = ys * 365 + ds;


        for (int i = 0; i < ms - 1; i++)
        {
            n1 += monthDays[i];
        }

        n1 += countLeapYears(ds,ms,ys);

        int n2 = yr * 365 + dr;
        for (int i = 0; i < mr - 1; i++)
        {
            n2 += monthDays[i];
        }
        n2 += countLeapYears(dr,mr,yr);

        return (n2 - n1);
    }

    public boolean isvalidphno(String s)
    {
        if(s.length()!=10)
            return false;
        boolean f=true;
        for(int i=0;i<10;i++){
            if(s.charAt(i)>'9' || s.charAt(i)<'0')
                f=false;
        }
        return f;  }

}

