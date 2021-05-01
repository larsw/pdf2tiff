package org.sral.pdf2tiff.cli;

import org.sral.pdf2tiff.PdfToMultiPageTIFFConverter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class CliApplication {
    static int RENDER_DPI = 300;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args) {
        PdfToMultiPageTIFFConverter converter = null;

        try {
            InputStream inputStream = null;
            ImageOutputStream outputStream = null;
            boolean piping = false;
            String inputFileName = null, outputFileName = null;

            if (args.length == 1 && args[0] != "-pipe") {
                inputStream = System.in;
                outputStream = ImageIO.createImageOutputStream(System.out);
                converter = new PdfToMultiPageTIFFConverter(inputStream, outputStream, RENDER_DPI);
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

                inputStream = new FileInputStream(inputFile);
                outputStream = ImageIO.createImageOutputStream(outputFile);
                converter = new PdfToMultiPageTIFFConverter(inputStream, outputStream, RENDER_DPI);
            } else {
                System.err.println("usage 1: pdf2tiff input.pdf output.tiff");
                System.err.println("usage 2: cat input.pdf | pdf2tiff -pipe > output.tiff");
                System.exit(-1);
            }

            if (!piping) {
                System.out.println("Converting '" + inputFileName + "' to a multi-page TIFF file named '"+ outputFileName +"'");
            }

            converter.convert((pageDetails) -> {
                System.out.println("Processing page " + pageDetails.getCurrentPage() + " of " + pageDetails.getTotalPages() + " page(s)");
            });

            if (!piping) {
                System.out.println("Done.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-4);
        } finally {
            if (converter != null) {
                try {
                    converter.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
