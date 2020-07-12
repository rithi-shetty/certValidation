import java.util.*;
import java.io.*;
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

class certValidationOcsp
{
	public static void main(String args[])
	{
		String execParam1, execParam2;
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		if(isWindows)
		{
			execParam1 = "cmd.exe";
			execParam2 = "/c";
		}
		else
		{
			execParam1 = "/bin/bash";
			execParam2 = "-c";
		}
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter cert directory");
		String ocspDir  = scanner.nextLine();
		System.out.println("Enter certificate file name(with extension)");
		String certFileName = scanner.nextLine();
		int fileNo = 1, flag = 1;
		String fileName = ocspDir+"/ocsp_uri.txt";
		String fileName2 = ocspDir+"/certStatus.txt";
		File nextFile;
		
		try 
		{
			final String[] cmd = {execParam1,execParam2,"cd " + ocspDir + ";csplit -f individual.pem "+ certFileName + " '/-----BEGIN CERTIFICATE-----/' '{*}'\n" };
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			
			do
			{
				final String[] cmd1 = {execParam1,execParam2,"cd " + ocspDir + ";openssl x509 -noout -ocsp_uri -in individual.pem0" + fileNo + " > ocsp_uri.txt  " };
				proc = Runtime.getRuntime().exec(cmd1);
				proc.waitFor();
				
				scanner = new Scanner(Paths.get(fileName), StandardCharsets.UTF_8.name());
				String content = scanner.useDelimiter("\n").next();
				scanner.close();
				
				final String[] cmd2 = {execParam1,execParam2,"cd " + ocspDir + ";openssl ocsp -issuer individual.pem0"+ (fileNo+1) + "  -cert individual.pem0"+ fileNo + "  -url " + content + " > certStatus.txt" };
			    proc = Runtime.getRuntime().exec(cmd2);
				proc.waitFor();
				
				scanner = new Scanner(Paths.get(fileName2), StandardCharsets.UTF_8.name());
				String content2 = scanner.useDelimiter("\n").next();
				scanner.close();
				
				String certStatus = "individual.pem0"+fileNo+": good";
				
				System.out.println();
				System.out.println();
				System.out.println("OUTPUT: ");
				
				if(!(certStatus.equals(content2)))
				{
					flag=0;
					System.out.println("NOT VALID");
					break;
				}
				fileNo++;
				
				nextFile = new File(ocspDir+"individual.pem0"+(fileNo+1)); 
	
			}while(nextFile.exists()==true);
			
			if(flag != 0) 
			{
				System.out.println("VALID");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		catch (Exception e)
		{
		    e.printStackTrace();
		}
	}
}	
