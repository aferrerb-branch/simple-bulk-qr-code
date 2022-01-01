package main.java;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;
import org.jfree.svg.ViewBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class qrs {

    public static void main(String[] args) {

        List <String> nameList2 = new ArrayList<>();
        List <String> dataList2 = new ArrayList<>();

        String home = System.getProperty("user.home");
        System.out.println("what is user home "+home);
        try (CSVReader csvReader = new CSVReader(new FileReader(home+ "/Downloads/FolderToTry/NL QR Code File 4.csv"))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                //records.add(Arrays.asList(values));
                if((!values[0].equals("NAME")) && (!values[1].equals("WEBSITE URL"))){
                    nameList2.add(values[0]);
                    dataList2.add(values[1]);
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }

        String[] nameList = new String[nameList2.size()];
        nameList = nameList2.toArray(nameList);
        String[] dataList = new String[dataList2.size()];
        dataList = dataList2.toArray(dataList);

        // make sure the folder is already available

        runAndGenerate(dataList, nameList);

    }

    public static void runAndGenerate(String[] dataList, String[] nameList){

        for (int i = 0; i < nameList.length; i++) {
            //generateQR(dataList[i], folder+"/"+nameList[i]+"_"+dataList[i]+".png");
            //generateQR(dataList[i], folder+"/"+nameList[i]);
            generateQR(dataList[i], nameList[i]);

        }
    }

    public static void generateQR(String data, String fileName){
        int size = 512;
        String fileType = "png";
        String home = System.getProperty("user.home");
        String filePath = home + "/Downloads/FolderToTry/QR/png/"+fileName; // Adding the png files to png folder
        String filePathPDF = home + "/Downloads/FolderToTry/QR/pdf/"+fileName;
        String filePathJPG = home + "/Downloads/FolderToTry/QR/jpeg/"+fileName;
        String filePathSVG = home + "/Downloads/FolderToTry/QR/svg/"+fileName;
        File myFile = new File(filePath+".png");
        File myJPGFile = new File(filePathJPG+".jpg");
        File mySVGFile = new File(filePathSVG+".svg");
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
            hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size,
                    size, hintMap);
            int width = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(width, width,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, width);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, fileType, myFile);

            // Now create the SVG file
            SVGGraphics2D g2 = new SVGGraphics2D(width, width);
            //BufferedImage qrCodeImage = getQRCode(targetUrl, width, height);
            g2.setColor(Color.BLACK);
            g2.drawImage(image, 0,0, width, width, null);
            //System.out.println("SVG file "+g2);

            ViewBox viewBox;
            //if ( withViewBox ){
            viewBox = new ViewBox(0,0,width,width);
            //}
            g2.getSVGElement(null, true, viewBox, null, null);
            SVGUtils.writeToSVG(new File(String.valueOf(mySVGFile)), g2.getSVGElement());


            // Now create the JPEG file
            BufferedImage newBufferedImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            // draw a white background and puts the originalImage on it.
            newBufferedImage.createGraphics()
                    .drawImage(image,
                            0,
                            0,
                            Color.WHITE,
                            null);

            // save image to file
            ImageIO.write(newBufferedImage, "jpg", myJPGFile);

            // now put the image inside a PDF document
            PDDocument doc;
            doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            try{
                PDImageXObject pdImageXObject = LosslessFactory.createFromImage(doc, image);
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, false);
                contentStream.drawImage(pdImageXObject, 200, 300, image.getWidth() / 2, image.getHeight() / 2);
                contentStream.close();
                doc.save( filePathPDF+".pdf" );
                doc.close();

            } catch (Exception io) {
                System.out.println(" -- fail --" + io);
            }
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
        //System.out.println("\nQR For ."+data+" Generated");
    }
}