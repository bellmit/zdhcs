package net.northking.cloudtest.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class ZipUtils

{
    /**
     * 替换某个 item,
     * @param zipInputStream zip文件的zip输入流
     * @param zipOutputStream 输出的zip输出流
     * @param itemName 要替换的 item 名称
     * @param itemInputStream 要替换的 item 的内容输入流
     */
    public static void replaceItem(ZipInputStream zipInputStream,
                                   ZipOutputStream zipOutputStream,
                                   String itemName,
                                   InputStream itemInputStream
    ){
        //
        if(null == zipInputStream){return;}
        if(null == zipOutputStream){return;}
        if(null == itemName){return;}
        if(null == itemInputStream){return;}
        //
        ZipEntry entryIn;
        try {
            while((entryIn = zipInputStream.getNextEntry())!=null)
            {
                String entryName =  entryIn.getName();
                ZipEntry entryOut = new ZipEntry(entryName);
                // 只使用 name
                zipOutputStream.putNextEntry(entryOut);
                // 缓冲区
                byte [] buf = new byte[8*1024];
                int len;

                if(entryName.equals(itemName)){
                    // 使用替换流
                    while((len = (itemInputStream.read(buf))) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                } else {
                    // 输出普通Zip流
                    while((len = (zipInputStream.read(buf))) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                }
                // 关闭此 entry
                zipOutputStream.closeEntry();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //e.printStackTrace();
            close(itemInputStream);
            close(zipInputStream);
            close(zipOutputStream);
        }
    }

    /**
     * 替换某个 item,
     * @param zipInputStream zip文件的zip输入流
     * @param zipOutputStream 输出的zip输出流
     * @param itemNames 要替换的 item 名称
     * @param itemInputFile 要替换的 item 的内容输入流
     */
    public static void replaceItems(ZipInputStream zipInputStream,
                                   ZipOutputStream zipOutputStream,
                                   String[] itemNames,
                                   String[] itemInputFile
    ){
        //
        if(null == zipInputStream){return;}
        if(null == zipOutputStream){return;}
        if(null == itemNames){return;}
        if(null == itemInputFile){return;}
        //
        ZipEntry entryIn;
        try {
            FileInputStream[] fileInputStreams = new FileInputStream[itemInputFile.length];
            for(int i=0; i<itemInputFile.length; i++) {
                File tempFile = new File(itemInputFile[i]);
                if(tempFile.exists()) {
                    FileInputStream itemInputStream = new FileInputStream(tempFile);
                    fileInputStreams[i] = itemInputStream;
                }else{
                    fileInputStreams[i] = null;
                }
            }
                while((entryIn = zipInputStream.getNextEntry())!=null) {
                    String entryName = entryIn.getName();
                    ZipEntry entryOut = new ZipEntry(entryName);
                    // 只使用 name
                    zipOutputStream.putNextEntry(entryOut);
                    // 缓冲区
                    byte[] buf = new byte[8 * 1024];
                    int len;

                    //命中哪个下标的数组
                    Integer index = null;
                    for(int i=0; i<itemNames.length; i++) {
                        if (entryName.equals(itemNames[i])) {
                            index = i;
                            break;
                        }
                    }
                    if (index != null) {
                        if(fileInputStreams[index] != null) {
                            // 使用替换流
                            while ((len = (fileInputStreams[index].read(buf))) > 0) {
                                zipOutputStream.write(buf, 0, len);
                            }
                        }
                    } else {
                        // 输出普通Zip流
                        while ((len = (zipInputStream.read(buf))) > 0) {
                            zipOutputStream.write(buf, 0, len);
                        }
                    }
                    // 关闭此 entry
                    zipOutputStream.closeEntry();
                }
            for(int i=0; i<itemInputFile.length; i++) {
                close(fileInputStreams[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //e.printStackTrace();
            close(zipInputStream);
            close(zipOutputStream);
        }
    }

    /**
     * 包装输入流
     */
    public static ZipInputStream wrapZipInputStream(InputStream inputStream){
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        return zipInputStream;
    }

    /**
     * 包装输出流
     */
    public static ZipOutputStream wrapZipOutputStream(OutputStream outputStream){
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        return zipOutputStream;
    }
    private static void close(InputStream inputStream){
        if (null != inputStream){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(OutputStream outputStream){
        if (null != outputStream){
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
