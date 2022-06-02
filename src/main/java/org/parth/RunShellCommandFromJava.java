package org.parth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.parth.Extractor.*;

public class RunShellCommandFromJava {
    private static ArrayList<Commit> getCommits(BufferedReader reader) throws IOException {

        ArrayList<Commit> allCommits=new ArrayList<>();
        String line = "i am parth";
        String id="";
        String name="";
        String date="";
        while((line = reader.readLine()) != null)
        {
            String t=getTypeof(line);
            if(t.equals("commit"))
            {
                id=extractid(line);
//                System.out.println(" : "+extractid(line));
            }

            else if(t.equals("Author:"))
            {
                name=extractAuthor(line);
//                System.out.println(" : "+extractAuthor(line));
            }
            else if(t.equals("Date:"))
            {
                date=extractDate(line);
//                System.out.println(" : "+extractDate(line)+"\n");

                Commit commit=new Commit(id,name,date);
                allCommits.add(commit);
            }

        }

        return allCommits;
    }
    private static String getTypeof(String s)
    {
        int n=s.length();
        String type="";
        for(int i=0;i<n;i++)
        {
            if(s.charAt(i)==' ') {
//                System.out.println("type of cmd is : "+type);
                return type;
            }
            type=type+s.charAt(i);
        }

        return type;
    }
    public static ArrayList<Commit> runCmd(String command) throws IOException, InterruptedException
    {

//        String command = "git log";

        Process proc = Runtime.getRuntime().exec(command);

        // Read the output

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        ArrayList<Commit> allCommits;
        allCommits=getCommits(reader);

        proc.waitFor();

        return allCommits;
    }
}
