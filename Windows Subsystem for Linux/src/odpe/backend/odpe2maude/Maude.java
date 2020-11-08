/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 *
 */
package odpe.backend.odpe2maude;

import java.io.*;
import java.nio.Buffer;
import java.util.Collection;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Maude {
    private Process maude;
    private File dir;
    private Collection<String> ignore_patterns;
    private String[] maudecmd;
	private InputStream maudeout;
	private BufferedWriter maudein;
	private Boolean start = false;

    public Maude(File dir, Collection<String> ignore_patterns,
    		String... files)
    			throws IOException, MaudeException {
    	this.dir = dir;
    	this.ignore_patterns = ignore_patterns;
    	maudecmd = new String[files.length+5];
    	/** CHANGE "./maude.linux64" TO "./maude.darwin64" FOR MacOS **/
    	maudecmd[0] = "./maude.linux64";
    	maudecmd[1] = "-no-banner";
    	maudecmd[2] = "-no-ansi-color";
    	maudecmd[3] = "-no-mixfix";
    	maudecmd[4] = "-no-wrap";
    	for(int i = 0; i < files.length; ++i) {
    		maudecmd[5+i] = files[i];
    	}
    	start_maude(dir, maudecmd);
    	readAnswer();
    }

    private void start_maude(File dir, String[] maudecmd) throws IOException {

		/** THESE TWO LINES RUN MAUDE AT THE BACKEND IN THE WINDOWS SUBSYSTEM FOR LINUX
		 *  UNCOMMENT THIS LINE IF YOU WANT TO USE THE WINDOWS SUBSYSTEM FOR LINUX
		 */
		String cmd = "\"".concat(String.join(" ",maudecmd)).concat("\"");
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "bash -c ".concat(cmd));

		/** UNCOMMENT THIS LINE IF YOU WANT TO USE NORMAL LINUX OR MacOS **/
		//ProcessBuilder pb = new ProcessBuilder(maudecmd);

		if(dir != null)
    		pb.directory(dir);
    	pb.redirectErrorStream(true);
    	maude = pb.start();
		maudeout = maude.getInputStream();
		maudein = new BufferedWriter(new OutputStreamWriter(maude.getOutputStream()));
        start = true;
    }


	private String readline() throws IOException, MaudeException {
		StringBuffer buf = new StringBuffer();
		String prompt = "Maude>";
		int l = prompt.length();
		int c = -1, i;

		long startTime = System.currentTimeMillis(); //fetch starting time
		while(maudeout.available() == 0){};

		for(i = 0; i < l; ++i) {
            c = maudeout.read();
            if (c == '\n' || c == '\r' || c == -1)
                break;
            buf.append((char) c);
		}
		if(i == l && buf.toString().equals("Maude>"))
			return null;
		if(c == '\n' || c == '\r' || c == -1)
			return buf.toString();
		for(c = maudeout.read(); c != '\n' && c != '\r' && c != -1; buf.append((char)c), c = maudeout.read());
		String res = buf.toString();
		if(res.startsWith("Warning"))
			throw new MaudeException(res);
		return res;
	}



    private Vector<String> readAnswer() throws IOException, MaudeException {
    	Vector<String> answer = new Vector<String>();
    	String str;

    	if (start) {
            start = false;
            return answer;
        }

    	while( maudeout.available() == 0 );

    	while((str = readline()) != null) {
			if (addLine(str)) {
				answer.add(str);
			}

			if (answer.size() > 2 && answer.get(2).startsWith("result ")){
				break;
			}
		}
    	return answer;
    }

    private boolean addLine(String str) {
    	if(str.contains("Advisory: "))
    		return false;
    	else if(str.contains("==========="))
    		return false;
    	else {
    		for(String p : ignore_patterns)
    			if(str.contains(p))
    				return false;
    		return true;
    	}
    }

    public Vector<String> ask(String question) throws IOException, MaudeException {
    	maudein.write(question, 0, question.length());
    	maudein.newLine();
    	maudein.flush();
		return readAnswer();
    }
 
 	public void stop() throws IOException, MaudeException  {
		maude.destroy();
		start_maude(dir, maudecmd);
		readAnswer();
	}

	public String reduce(String module, String term) throws IOException, MaudeException {
		Vector<String> v = ask("reduce in " + module + " : " + term + " .");
		if (!v.get(0).startsWith("reduce "))
			throw new MaudeException("expected `reduce', found "+v.get(0));
		if (!v.get(1).startsWith("rewrites: "))
			throw new MaudeException("expected information about rewrites, found "+v.get(1));
		Matcher m = Pattern.compile("^result .+: (.+)$").matcher(v.get(2));
		
		if (m.matches()) {
			return m.group(1);
		}
		throw new MaudeException("expected result, found "+v.get(3));
	}

}
