package odpe.util;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class Automate {

	/**
	 * @throws IOException
	 * @throws AWTException
	 * @throws InterruptedException
	 *
	 *
	*/
	
	 private String[] maudecmd;
	 
	public static void main(String[] args)throws InterruptedException, AWTException
	{
		Runtime r = Runtime.getRuntime();
		Process p;

		String username = "jl2553";
		String password = "Villa2208";
		String serverString = "linux.bath.ac.uk";
		String s = "C:\\"+"Program Files (x86)"+"\\KiTTY\\kitty.exe -ssh -l "+username+" -pw "+password+" "+serverString+"";
		try
		{
			//open the putty session with the above given username, password and server
			p = r.exec(s);
			Thread.sleep(500);

		} catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();

		}
		Thread.sleep(100);
		
		Robot robot = new Robot();
		sendKeys(robot, "cd eclipse/odpe/");
		robot.keyPress(KeyEvent.VK_ENTER);
		sendKeys(robot, "./maude.linux64 -no-banner -no-ansi-color -no-mixfix -no-wrap");
		robot.keyPress(KeyEvent.VK_ENTER);
		/*
		robot.keyPress(KeyEvent.VK_S);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_U);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_D);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_O);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_SPACE);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_S);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_U);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_SPACE);
		Thread.sleep(150);
		robot.keyPress(109);
		Thread.sleep(150);
		robot.keyPress(KeyEvent.VK_ENTER);
		Thread.sleep(150);
	*/ 

	}
	
	
	public static void test() throws InterruptedException, AWTException {
		Runtime r = Runtime.getRuntime();
		Process p;

		String username = "jl2553";
		String password = "Villa2208";
		String serverString = "linux.bath.ac.uk";
		String s = "C:\\"+"Program Files (x86)"+"\\KiTTY\\kitty.exe -ssh -l "+username+" -pw "+password+" "+serverString+"";
		try
		{
			//open the putty session with the above given username, password and server
			p = r.exec(s);
			Thread.sleep(500);

		} catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();

		}
		Thread.sleep(100);
		
		
		
		Robot robot = new Robot();
		sendKeys(robot, "cd eclipse/odpe/");
		robot.keyPress(KeyEvent.VK_ENTER);
		sendKeys(robot, "./maude.linux64 -no-banner -no-ansi-color -no-mixfix -no-wrap");
		robot.keyPress(KeyEvent.VK_ENTER);
	}
	
	
	
	
	private static void sendKeys(Robot robot, String keys) {
	    for (char c : keys.toCharArray()) {
	        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
	        if (KeyEvent.CHAR_UNDEFINED == keyCode) {
	            throw new RuntimeException(
	                "Key code not found for character '" + c + "'");
	        }
	        robot.keyPress(keyCode);
	        robot.delay(1);
	        robot.keyRelease(keyCode);
	        robot.delay(1);
	    }
	}
}