package org.sral.pdf2tiff;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

public class Pdf2tiffApplication {
    static int RENDER_DPI = 300;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args) {

        InputStream fis = null;
        ImageOutputStream ios = null;
        boolean piping = false;
        String inputFileName = null, outputFileName = null;
        try {
            if (args.length == 1 && args[0] != "-pipe") {
                fis = System.in;
                ios = ImageIO.createImageOutputStream(System.out);
                piping = true;
            } else if (args.length == 2) {
                inputFileName = args[0];
                outputFileName = args[1];
                File inputFile = new File(inputFileName);
                File outputFile = new File(outputFileName);

                if (!inputFile.exists()) {
                    System.err.println("The input file '" + outputFile + "' does not exist.");
                    System.exit(-2);
                }

                if (outputFile.exists()) {
                    System.err.println("The output file '" + outputFile + "' already exists.");
                    System.exit(-3);
                }

                fis = new FileInputStream(inputFile);
                ios = ImageIO.createImageOutputStream(outputFile);
            } else {
                System.err.println("usage 1: pdf2tiff input.pdf output.tiff");
                System.err.println("usage 2: cat input.pdf | pdf2tiff -pipe > output.tiff");
                System.exit(-1);
            }

            PDDocument document = PDDocument.load(fis);

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
            ImageWriter imageWriter = writers.next();

            imageWriter.setOutput(ios);
            imageWriter.prepareWriteSequence(null);

            if (!piping) {
                System.out.println("Converting '" + inputFileName + "' to a multi-page TIFF file named '"+ outputFileName +"'");
            }
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                if (!piping) {
                    int page = i+1;
                    System.out.println("Processing page " + page + " of " + document.getNumberOfPages());
                }
                BufferedImage image = renderer.renderImageWithDPI(i, RENDER_DPI);
                ImageWriteParam param = imageWriter.getDefaultWriteParam();
                IIOMetadata metadata = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                TIFFUtil.setCompressionType(param, image);
                TIFFUtil.updateMetadata(metadata, image, RENDER_DPI);
                imageWriter.writeToSequence(new IIOImage(image, null, metadata), param);
            }
            imageWriter.endWriteSequence();
            imageWriter.dispose();
            ios.flush();
            ios.close();
            if (!piping) {
                System.out.println("Done.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-4);
        }
    }
}
