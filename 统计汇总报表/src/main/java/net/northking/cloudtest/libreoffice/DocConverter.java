package net.northking.cloudtest.libreoffice;

import java.io.BufferedReader;

import java.io.File;

import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;
import java.util.Date;

import com.artofsolving.jodconverter.DocumentConverter;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;

import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;

import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import net.northking.cloudtest.service.impl.ProgressReportServiceImpl;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.ExternalOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocConverter {

	private final static Logger logger = LoggerFactory.getLogger(DocConverter.class);

	private static final int environment = 2;// 环境1：windows,2:linux(涉及pdf2swf路径问题)

	private String fileString;

	private String outputPath = "";// 输入路径，如果不设置就输出在默认位置

	private String fileName;

	private File pdfFile;

	private File swfFile;

	private File docFile;

	public DocConverter(String fileString) {

		ini(fileString);

	}

	/*
	 * 
	 * 重新设置 file @param fileString
	 */

	public void setFile(String fileString) {

		ini(fileString);

	}

	/*
	 * 
	 * 初始化 @param fileString
	 */

	private void ini(String fileString) {

		this.fileString = fileString;

		fileName = fileString.substring(0, fileString.lastIndexOf("."));

		docFile = new File(fileString);

		pdfFile = new File(fileName + ".pdf");

		swfFile = new File(fileName + ".swf");

	}

	/*
	 * 
	 * 转为PDF @param file
	 */

	private void doc2pdf() throws Exception {

		if (docFile.exists()) {

			//if (!pdfFile.exists()) {
				String libreOfficePath = "/opt/libreoffice5.4";
				int libreOfficePort = 8102;
				OfficeManager officeManager = null;
				try {
					ExternalOfficeManagerConfiguration externalProcessOfficeManager = new ExternalOfficeManagerConfiguration();
					externalProcessOfficeManager.setConnectOnStart(true);
					externalProcessOfficeManager.setPortNumber(libreOfficePort);
					officeManager = externalProcessOfficeManager.buildOfficeManager();
					officeManager.start();
					logger.info("已获取libreoffice连接");
				} catch (Exception ex) {
					DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
					configuration.setOfficeHome(libreOfficePath);
					configuration.setPortNumbers(libreOfficePort);
					configuration.setTaskExecutionTimeout(1000 * 60 * 5L);
					configuration.setTaskQueueTimeout(1000 * 60 * 60 * 24L);
					officeManager = configuration.buildOfficeManager();
					officeManager.start();
					logger.warn("重新获取libreoffice连接");
				} finally {
					logger.info("libreoffice转换文件中：{}-{}", docFile, docFile.exists());
					OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
					converter.convert(docFile, pdfFile);
					logger.info("libreoffice转换文件完成：{}-{}", pdfFile, pdfFile.exists());
					if(officeManager != null){
						officeManager.stop();
					}
				}

			//} else {

				//System.out.println("****已经转换为pdf，不需要再进行转化****");

			//}

		} else {

			System.out.println("****swf转换器异常，需要转换的文档不存在，无法转换****");

		}

	}

	/*
	 * 
	 * 转换成swf
	 */

	private void pdf2swf() throws Exception {

		Runtime r = Runtime.getRuntime();
		logger.info("swf开始转换...." + swfFile);
		//if (!swfFile.exists()) {

			if (pdfFile.exists()) {

				if (environment == 1)// windows环境处理

				{

					try {

						// 这里根据SWFTools安装路径需要进行相应更改

						Process p = r.exec("d:/Program Files (x86)/SWFTools/pdf2swf.exe "
								+ pdfFile.getPath() + " -o "
								+ swfFile.getPath() + " -T 9 -t -s storeallcharacters");

						System.out.print(loadStream(p.getInputStream()));

						System.err.print(loadStream(p.getErrorStream()));

						System.out.print(loadStream(p.getInputStream()));

						System.err.println("****swf转换成功，文件输出："
								+ swfFile.getPath() + "****");

						if (pdfFile.exists()) {

							pdfFile.delete();

						}

					} catch (Exception e) {

						e.printStackTrace();

						throw e;

					}

				} else if (environment == 2)// linux环境处理

				{

					try {
						Process p = r.exec("/usr/local/swftools/bin/pdf2swf " + pdfFile.getPath()
								+ " -o " + swfFile.getPath() + " -T 9 -t -s storeallcharacters");

						System.out.print(loadStream(p.getInputStream()));

						System.err.print(loadStream(p.getErrorStream()));

						System.err.println("****swf转换成功，文件输出："
								+ swfFile.getPath() + "****");

						if (pdfFile.exists()) {

							//pdfFile.delete();

						}

					} catch (Exception e) {
						logger.error("error:", e);
						e.printStackTrace();

						throw e;

					}

				}

			} else {

				System.out.println("****pdf不存在，无法转换****");

			}

		//} else {

			//System.out.println("****swf已存在不需要转换****");

		//}

	}

	static String loadStream(InputStream in) throws IOException {

		int ptr = 0;

		// 把InputStream字节流 替换为BufferedReader字符流 2013-07-17修改

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringBuilder buffer = new StringBuilder();

		while ((ptr = reader.read()) != -1) {

			buffer.append((char) ptr);

		}

		return buffer.toString();

	}

	/*
	 * 
	 * 转换主方法
	 */

	public boolean conver() {

		/*if (swfFile.exists()) {

			System.out.println("****swf转换器开始工作，该文件已经转换为swf****");

			return true;

		}*/

		if (environment == 1) {

			System.out.println("****swf转换器开始工作，当前设置运行环境windows****");

		} else {

			System.out.println("****swf转换器开始工作，当前设置运行环境linux****");

		}

		try {

			doc2pdf();

			pdf2swf();

		} catch (Exception e) {

			// TODO: Auto-generated catch block

			e.printStackTrace();

			return false;

		}

		if (swfFile.exists()) {

			return true;

		} else {

			return false;

		}

	}

	/*
	 * 
	 * 返回文件路径 @param s
	 */

	public String getswfPath() {

		if (swfFile.exists()) {

			String tempString = swfFile.getPath();

			tempString = tempString.replaceAll("\\\\", "/");

			return tempString;

		} else {

			return "";

		}

	}

	/*
	 * 
	 * 设置输出路径
	 */

	public void setOutputPath(String outputPath) {

		this.outputPath = outputPath;

		if (!outputPath.equals("")) {

			String realName = fileName.substring(fileName.lastIndexOf("/"),
					fileName.lastIndexOf("."));

			if (outputPath.charAt(outputPath.length()) == '/') {

				swfFile = new File(outputPath + realName + ".swf");

			} else {

				swfFile = new File(outputPath + realName + ".swf");

			}

		}

	}

	public static void main(String s[]) {

		DocConverter d = new DocConverter("D:\\工作\\公司\\南航驻场项目人员守则.doc");

		d.conver();

	}

}
