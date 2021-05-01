package org.sral.pdf2tiff;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfToMultiPageTIFFConverter implements AutoCloseable {

    private InputStream inputStream;
    private ImageOutputStream outputStream;
    private int renderDpi = 150;

    public PdfToMultiPageTIFFConverter(String inputFileName, String outputFileName,  int renderDpi) throws IOException {

        if (inputFileName == null) throw new IllegalArgumentException("inputFile");
        if (outputFileName == null) throw new IllegalArgumentException("outputFile");
        if (renderDpi < 0) throw new InvalidClassException("renderDpi");

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

        this.inputStream = new FileInputStream(inputFile);
        this.outputStream = ImageIO.createImageOutputStream(outputFile);
        this.renderDpi = renderDpi;
    }

    public PdfToMultiPageTIFFConverter(InputStream inputStream, ImageOutputStream outputStream, int renderDpi) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.renderDpi = renderDpi;
    }

    public void convert(Consumer<PageDetails> pageProcessingCallback) throws IOException {

        PDDocument document = PDDocument.load(this.inputStream);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
        ImageWriter imageWriter = writers.next();

        imageWriter.setOutput(this.outputStream);
        imageWriter.prepareWriteSequence(null);

        PDFRenderer renderer = new PDFRenderer(document);
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            if (pageProcessingCallback != null) {
                pageProcessingCallback.accept(new PageDetails(i+1, document.getNumberOfPages()));
            }

            BufferedImage image = renderer.renderImageWithDPI(i, renderDpi);
            ImageWriteParam param = imageWriter.getDefaultWriteParam();
            IIOMetadata metadata = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            TIFFUtil.setCompressionType(param, image);
            TIFFUtil.updateMetadata(metadata, image, renderDpi);
            imageWriter.writeToSequence(new IIOImage(image, null, metadata), param);
        }
        imageWriter.endWriteSequence();
        imageWriter.dispose();
        this.outputStream.flush();
    }

    @Override
    public void close() throws Exception {
        this.inputStream.close();
        this.outputStream.close();
    }
}
