import java.util.*;
import java.io.*;
import java.lang.*;
import java.security.*;
import java.security.KeyStore;
import java.security.cert.*;
import java.math.BigInteger;
import javax.security.auth.x500.X500Principal; 
class certValidationCrl
{
	public static void main(String args[])
	{
		String execParam1, execParam2;
		String crlDir,crlFileName,certDir,certFileName;
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
		
		Scanner input = new Scanner(System.in);
		
		System.out.println("Enter crl directory");
		crlDir = input.nextLine();
		
		System.out.println("Enter CRL file name(without extension)");
		crlFileName = input.nextLine();
		
		System.out.println("Enter cert directory");
		certDir = input.nextLine();
		
		System.out.println("Enter certificate file name(without extension)");
		certFileName = input.nextLine();
		
		try 
		{
			final String[] cmd = {execParam1,execParam2,"cd " + crlDir + "; openssl crl -in " + crlFileName + ".crl -out " + crlFileName + ".pem; openssl crl -inform der -in " + crlFileName + ".crl -out " + crlFileName + ".pem" };
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			X509CRL crl=null;
			
			System.out.println();
			System.out.println();
			System.out.println("OUTPUT: ");
			
//----------------------CRL------------------------
			InputStream inStreamCRL=null;
			inStreamCRL = new FileInputStream(crlDir + "/" + crlFileName + ".pem");
			
			CertificateFactory certFactoryCRL=null;
			certFactoryCRL = CertificateFactory.getInstance("X.509");
			crl = (X509CRL)certFactoryCRL.generateCRL(inStreamCRL);
			inStreamCRL.close();

//----------------------CERT----------------------------     
			int flag=0;
			X509Certificate cert = null;
			FileInputStream fileInstreamCert=null;
			fileInstreamCert = new FileInputStream(certDir + "/" + certFileName + ".pem");
			BufferedInputStream bufInstreamCert  = new BufferedInputStream(fileInstreamCert);
			
			CertificateFactory certFactory=null;
			certFactory = CertificateFactory.getInstance("X.509");
		
			while (bufInstreamCert.available() > 0)
			{
			    cert = (X509Certificate)certFactory.generateCertificate(bufInstreamCert);
				BigInteger certSerialNo = cert.getSerialNumber();
				if(crl.getRevokedCertificate(certSerialNo)!=null)
				{
					System.out.println("CHAIN_INVALID");
					flag=1;
					break;
				}
				
			}
			
			String issuerName= cert.getIssuerX500Principal().toString();
			String subjectName= cert.getSubjectX500Principal().toString();
			
			if(flag!=1)
			{
				if(issuerName.equals(subjectName))
				{
					System.out.println("CHAIN_VALID");
					System.exit(0);
				}
				else
				{
					System.out.println("CHAIN_INVALID");
				}
			}
		
			
        }

		catch (CertificateException e) 
		{
			e.printStackTrace();
		}
		
		catch (CRLException e) 
		{
			e.printStackTrace();
		}
		
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
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







