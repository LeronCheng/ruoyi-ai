package org.ruoyi.chat.util;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C), 2023-2025 CONE
 * Author: LaiRongCheng
 * Date: 2025-05-21 15:04
 * FileName: SvgToppt
 * Description: SVG文件转PPT工具
 *
 * @version <strong>v1.0</strong><br>
 * <br>
 * <strong>修改历史:</strong><br>
 * 修改人    修改日期    修改描述<br>
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
public class SvgToppt {

    // 存储SVG元素到PPT对象的映射关系
    private static final Map<String, Color> colorCache = new HashMap<>();

    // Transform相关的正则表达式
    private static final Pattern translatePattern = Pattern.compile("translate\\(\\s*([\\d.-]+)(?:[,\\s]+([\\d.-]+))?\\s*\\)");
    /**
     * 将SVG文件转换为PPT
     *
     * @param svgFilePath SVG文件路径
     * @param outputPath 输出PPT文件路径
     * @throws Exception 如果转换过程中发生错误
     */
    public static void convertSvgToPpt(String svgFilePath, String outputPath) throws Exception {
        // 创建新的PPT演示文稿
        XMLSlideShow ppt = new XMLSlideShow();

        // 读取SVG文件
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        Document svgDoc;

        if (Files.exists(Paths.get(svgFilePath))) {
            svgDoc = factory.createDocument("file:" + new File(svgFilePath).toURI().getPath(), new FileInputStream(svgFilePath));
        } else {
            // 尝试从资源路径加载
            Path resourcePath = Paths.get(SvgToppt.class.getClassLoader().getResource(svgFilePath).toURI());
            svgDoc = factory.createDocument("file:" + resourcePath.toUri().getPath(), new FileInputStream(resourcePath.toFile()));
        }

        // 获取SVG尺寸
        Element svgElement = svgDoc.getDocumentElement();
        double width = Double.parseDouble(svgElement.getAttribute("width"));
        double height = Double.parseDouble(svgElement.getAttribute("height"));

        // 设置PPT幻灯片尺寸
        ppt.setPageSize(new Dimension((int)width, (int)height));

        // 创建幻灯片
        XSLFSlide slide = ppt.createSlide();

        // 遍历SVG元素并转换为PPT元素
        processSvgElement(svgElement, slide, 0, 0);

        // 保存PPT文件
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            ppt.write(out);
        }
    }

    /**
     * 先将SVG文件读取为字符串，再用字符串解析SVG并生成PPT
     */
    public static XMLSlideShow convertToPptBySvgString(String svgContent) throws Exception {
        // 1. 读取SVG文件为字符串
//        String svgContent = new String(Files.readAllBytes(Paths.get(svgFilePath)), "UTF-8");

        // 2. 用字符串解析SVG并生成PPT
        String parser = org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName();
        org.apache.batik.anim.dom.SAXSVGDocumentFactory factory = new org.apache.batik.anim.dom.SAXSVGDocumentFactory(parser);
        Document svgDoc = factory.createDocument(null, new java.io.StringReader(svgContent));

        // 3. 后续流程和原来一样
        Element svgElement = svgDoc.getDocumentElement();
        double width = Double.parseDouble(svgElement.getAttribute("width"));
        double height = Double.parseDouble(svgElement.getAttribute("height"));

        XMLSlideShow ppt = new XMLSlideShow();
        ppt.setPageSize(new Dimension((int)width, (int)height));
        XSLFSlide slide = ppt.createSlide();
        processSvgElement(svgElement, slide, 0, 0);
        return ppt;
//        try (FileOutputStream out = new FileOutputStream(outputPath)) {
//            ppt.write(out);
//        }
    }

    /**
     * 处理SVG元素，将其转换为PPT元素
     *
     * @param element SVG元素
     * @param slide PPT幻灯片
     * @param parentTranslateX 父元素的X轴变换
     * @param parentTranslateY 父元素的Y轴变换
     */
    private static void processSvgElement(Element element, XSLFSlide slide, double parentTranslateX, double parentTranslateY) {
        // 解析当前元素的transform
        double translateX = parentTranslateX;
        double translateY = parentTranslateY;

        String transform = element.getAttribute("transform");
        if (transform != null && !transform.isEmpty()) {
            // 解析transform属性
            Matcher translateMatcher = translatePattern.matcher(transform);
            if (translateMatcher.find()) {
                translateX += Double.parseDouble(translateMatcher.group(1));
                if (translateMatcher.group(2) != null) {
                    translateY += Double.parseDouble(translateMatcher.group(2));
                }
            }
        }

        // 获取所有子元素
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String tagName = child.getTagName();

                switch (tagName) {
                    case "rect":
                        createRectangle(child, slide, translateX, translateY);
                        break;
                    case "text":
                        createText(child, slide, translateX, translateY);
                        break;
                    case "line":
                        createLine(child, slide, translateX, translateY);
                        break;
                    case "circle":
                        createCircle(child, slide, translateX, translateY);
                        break;
                    case "path":
                        createPath(child, slide, translateX, translateY);
                        break;
                    case "polyline":
                        createPolyline(child, slide, translateX, translateY);
                        break;
                    case "g":
                        // 递归处理分组内元素，传递累计的transform
                        processSvgElement(child, slide, translateX, translateY);
                        break;
                }
            }
        }
    }

    /**
     * 创建矩形元素
     */
    private static void createRectangle(Element element, XSLFSlide slide, double translateX, double translateY) {
        try {
            double x = parseDoubleAttribute(element, "x", 0) + translateX;
            double y = parseDoubleAttribute(element, "y", 0) + translateY;
            double width = parseDoubleAttribute(element, "width", 0);
            double height = parseDoubleAttribute(element, "height", 0);
            double rx = parseDoubleAttribute(element, "rx", 0);
            double ry = parseDoubleAttribute(element, "ry", 0);

            XSLFAutoShape shape;
            if (rx > 0 || ry > 0) {
                // 圆角矩形
                shape = slide.createAutoShape();
                shape.setShapeType(ShapeType.ROUND_RECT);
            } else {
                // 普通矩形
                shape = slide.createAutoShape();
                shape.setShapeType(ShapeType.RECT);
            }

            // 设置位置和大小
            shape.setAnchor(new Rectangle2D.Double(x, y, width, height));

            // 设置填充颜色
            String fill = element.getAttribute("fill");
            if (!fill.isEmpty() && !fill.equals("none")) {
                shape.setFillColor(parseColor(fill));
            } else {
                shape.setFillColor(null);
            }

            // 设置透明度
            String opacity = element.getAttribute("opacity");
            if (!opacity.isEmpty()) {
                double alpha = Double.parseDouble(opacity);
                shape.setFillColor(applyOpacity(shape.getFillColor(), alpha));
            }

            // 设置边框
            String stroke = element.getAttribute("stroke");
            if (!stroke.isEmpty() && !stroke.equals("none")) {
                shape.setLineColor(parseColor(stroke));

                // 边框宽度
                String strokeWidth = element.getAttribute("stroke-width");
                if (!strokeWidth.isEmpty()) {
                    double lineWidth = Double.parseDouble(strokeWidth);
                    shape.setLineWidth(lineWidth);
                }
            } else {
                shape.setLineWidth(0); // 无边框
            }
        } catch (Exception e) {
            System.err.println("创建矩形元素时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建文本元素
     */
    private static void createText(Element element, XSLFSlide slide, double translateX, double translateY) {
        try {
            NodeList children = element.getChildNodes();
            ArrayList<Element> elements = new ArrayList<>();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && "tspan".equals(((Element) node).getTagName())) {
                    elements.add((Element) node);
                }
            }
            if (elements.size() > 0) {
                createTextWithTspan(element,elements, slide, translateX, translateY);
                return;
            }
            double x = parseDoubleAttribute(element, "x", 0) + translateX;
            double y = parseDoubleAttribute(element, "y", 0) + translateY;

            double gap = 0;
            // 获取文本内容
            String textContent = element.getTextContent().trim();
            String textAnchor = element.getAttribute("text-anchor");
            // 获取字体大小
            double fontSize = 12; // 默认字体大小
            String fontSizeStr = element.getAttribute("font-size");
            if (!fontSizeStr.isEmpty()) {
                fontSize = Double.parseDouble(fontSizeStr.replaceAll("px", "").trim());
            }

            gap = fontSize;
//            gap *= 1.2;
            gap += 17;
            //去小数点
            gap = Math.round(gap);

            // 计算文本框宽度
            double textWidth = calculateTextWidth(textContent, fontSize);

            XSLFTextBox textBox = slide.createTextBox();
            if ("middle".equals(textAnchor)) {
                x = x - textWidth / 2;
            }
            textBox.setAnchor(new Rectangle2D.Double(x, y - gap, textWidth, fontSize * 2)); // 动态设置宽度 // 设置初始大小，后面会自动调整

            XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
            XSLFTextRun textRun = paragraph.addNewTextRun();

            textRun.setText(textContent);
            textRun.setFontSize(fontSize);

            // 设置字体
            String fontFamily = element.getAttribute("font-family");
            if (!fontFamily.isEmpty()) {
                fontFamily = fontFamily.split(",")[0].replaceAll("'", "").trim();
                textRun.setFontFamily(fontFamily);
            }

            // 设置字体大小
//            fontSize = element.getAttribute("font-size");
//            if (!fontSize.isEmpty()) {
//                double size = Double.parseDouble(fontSize.replaceAll("px", "").trim());
//                textRun.setFontSize(size);
//            }

            // 设置加粗
            String fontWeight = element.getAttribute("font-weight");
            if ("bold".equals(fontWeight)) {
                textRun.setBold(true);
            }

            // 设置文字颜色
            String fill = element.getAttribute("fill");
            if (!fill.isEmpty()) {
                textRun.setFontColor(parseColor(fill));
            }

            // 设置文本对齐方式
//            paragraph.setTextAlign(TextParagraph.TextAlign.LEFT);

            if ("middle".equals(textAnchor)) {
                paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
            } else if ("end".equals(textAnchor)) {
                paragraph.setTextAlign(TextParagraph.TextAlign.RIGHT);
            } else {
                paragraph.setTextAlign(TextParagraph.TextAlign.LEFT);
            }

            // 自动调整文本框大小
            textBox.setWordWrap(true);
        } catch (Exception e) {
            System.err.println("创建文本元素时发生错误: " + e.getMessage());
        }
    }

    /**
     * 支持 tspan 多行文本和换行
     */
    private static void createTextWithTspan(Element element, ArrayList<Element> elements, XSLFSlide slide, double translateX, double translateY) {
        try {
            double x = parseDoubleAttribute(element, "x", 0) + translateX;
            double y = parseDoubleAttribute(element, "y", 0) + translateY;
            double fontSize = 12;
            String fontSizeStr = element.getAttribute("font-size");
            if (!fontSizeStr.isEmpty()) {
                fontSize = Double.parseDouble(fontSizeStr.replaceAll("px", "").trim());
            }
            String fontFamily = element.getAttribute("font-family");
            String fill = element.getAttribute("fill");
            y = y - 17;
            double maxWidth = 0;
            for (Element tspan : elements) {
                XSLFTextBox textBox = slide.createTextBox();
                String tspanText = tspan.getTextContent().trim();
                String tspanX = tspan.getAttribute("x");
                String tspanDy = tspan.getAttribute("dy");
                String tspany = tspan.getAttribute("y");
                XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
                XSLFTextRun textRun = paragraph.addNewTextRun();
                textRun.setText(tspanText);
                textRun.setFontSize(fontSize);
                if (!fontFamily.isEmpty()) textRun.setFontFamily(fontFamily);
                if (!fill.isEmpty()) textRun.setFontColor(parseColor(fill));
                paragraph.setTextAlign(TextParagraph.TextAlign.LEFT);
                // 记录最大宽度
                double textWidth = calculateTextWidth(tspanText, fontSize);
                if (textWidth > maxWidth) maxWidth = textWidth;
                // 设置文本框位置和大小（高度自适应）
                double dx = Double.parseDouble(tspanX);
                double ddy = 0;
                double dy = 0;
                if (!tspanDy.isEmpty()) {
                    ddy = Double.parseDouble(tspanDy);
                }
                if (!tspany.isEmpty()) {
                    dy = Double.parseDouble(tspany);
                }
                y += ddy;
                double width = calculateTextWidth(tspanText, fontSize);
                textBox.setAnchor(new Rectangle2D.Double(x + dx, y + dy - fontSize, width, fontSize * 2));
                textBox.setWordWrap(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建线条元素
     */
    private static void createLine(Element element, XSLFSlide slide, double translateX, double translateY) {
        try {
            double x1 = parseDoubleAttribute(element, "x1", 0) + translateX;
            double y1 = parseDoubleAttribute(element, "y1", 0) + translateY;
            double x2 = parseDoubleAttribute(element, "x2", 0) + translateX;
            double y2 = parseDoubleAttribute(element, "y2", 0) + translateY;

            XSLFConnectorShape line = slide.createConnector();
            line.setAnchor(new Rectangle2D.Double(
                    Math.min(x1, x2),
                    Math.min(y1, y2),
                    Math.abs(x2 - x1),
                    Math.abs(y2 - y1)
            ));

            // 设置线条颜色
            String stroke = element.getAttribute("stroke");
            if (!stroke.isEmpty()) {
                line.setLineColor(parseColor(stroke));
            }

            // 设置线条宽度
            String strokeWidth = element.getAttribute("stroke-width");
            if (!strokeWidth.isEmpty()) {
                double width = Double.parseDouble(strokeWidth);
                line.setLineWidth(width);
            }

            // 线条不支持直接设置起点和终点，我们通过调整锚点位置来实现
        } catch (Exception e) {
            System.err.println("创建线条元素时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建圆形元素
     */
    private static void createCircle(Element element, XSLFSlide slide, double translateX, double translateY) {
        try {
            double cx = parseDoubleAttribute(element, "cx", 0) + translateX;
            double cy = parseDoubleAttribute(element, "cy", 0) + translateY;
            double r = parseDoubleAttribute(element, "r", 0);

            XSLFAutoShape circle = slide.createAutoShape();
            circle.setShapeType(ShapeType.ELLIPSE);
            circle.setAnchor(new Rectangle2D.Double(cx - r, cy - r, 2 * r, 2 * r));

            // 设置填充颜色
            String fill = element.getAttribute("fill");
            if (!fill.isEmpty() && !fill.equals("none")) {
                circle.setFillColor(parseColor(fill));
            }

            // 设置透明度
            String opacity = element.getAttribute("opacity");
            if (!opacity.isEmpty()) {
                double alpha = Double.parseDouble(opacity);
                circle.setFillColor(applyOpacity(circle.getFillColor(), alpha));
            }

            // 设置边框
            String stroke = element.getAttribute("stroke");
            if (!stroke.isEmpty() && !stroke.equals("none")) {
                circle.setLineColor(parseColor(stroke));

                // 边框宽度
                String strokeWidth = element.getAttribute("stroke-width");
                if (!strokeWidth.isEmpty()) {
                    double lineWidth = Double.parseDouble(strokeWidth);
                    circle.setLineWidth(lineWidth);
                }
            } else {
                circle.setLineWidth(0); // 无边框
            }
        } catch (Exception e) {
            System.err.println("创建圆形元素时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建路径元素
     */
    private static void createPath(Element element, XSLFSlide slide, double translateX, double translateY) {
        try {
            String d = element.getAttribute("d");
            if (d == null || d.isEmpty()) {
                return;
            }

            // 新增：创建Path2D对象
            java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();

            // 解析路径数据
            String[] commands = d.split("(?=[MLHVCSQTAZmlhvcsqtaz])");
            Point2D.Double currentPoint = new Point2D.Double(0, 0);
            Point2D.Double startPoint = null;

            for (String cmd : commands) {
                if (cmd.isEmpty()) continue;

                char command = cmd.charAt(0);
                String[] params = cmd.substring(1).trim().split("[,\\s]+");

                switch (command) {
                    case 'M': // 移动到
                        currentPoint.x = Double.parseDouble(params[0]) + translateX;
                        currentPoint.y = Double.parseDouble(params[1]) + translateY;
                        path.moveTo(currentPoint.x, currentPoint.y);
                        startPoint = new Point2D.Double(currentPoint.x, currentPoint.y);
                        break;
                    case 'L': // 画线到
                        double x = Double.parseDouble(params[0]) + translateX;
                        double y = Double.parseDouble(params[1]) + translateY;
                        path.lineTo(x, y);
                        currentPoint.x = x;
                        currentPoint.y = y;
                        break;
                    case 'H': // 水平线到
                        x = Double.parseDouble(params[0]) + translateX;
                        path.lineTo(x, currentPoint.y);
                        currentPoint.x = x;
                        break;
                    case 'V': // 垂直线到
                        y = Double.parseDouble(params[0]) + translateY;
                        path.lineTo(currentPoint.x, y);
                        currentPoint.y = y;
                        break;
                    case 'A': // 椭圆弧到
                        double rx = Double.parseDouble(params[0]);
                        double ry = Double.parseDouble(params[1]);
                        double angle = Math.toRadians(Double.parseDouble(params[2]));
                        int largeArcFlag = Integer.parseInt(params[3]);
                        int sweepFlag = Integer.parseInt(params[4]);
                        x = Double.parseDouble(params[5]) + translateX;
                        y = Double.parseDouble(params[6]) + translateY;
                        //System.out.println("A命令参数: rx=" + rx + ", ry=" + ry + ", angle=" + Math.toDegrees(angle) + ", largeArcFlag=" + largeArcFlag + ", sweepFlag=" + sweepFlag + ", x=" + x + ", y=" + y);
                        arcToBezierPureJava(path, currentPoint.x, currentPoint.y, x, y, rx, ry, angle, largeArcFlag, sweepFlag);
                        currentPoint.x = x;
                        currentPoint.y = y;
                        break;
                    case 'Z': // 闭合路径
                        path.closePath();
                        break;
                }
            }

            // 新增：将path渲染到PPT
            XSLFFreeformShape shape = slide.createFreeform();
            shape.setPath(path);

            // 设置填充颜色
            String fill = element.getAttribute("fill");
            if (!fill.isEmpty() && !fill.equals("none")) {
                shape.setFillColor(parseColor(fill));
            } else {
                shape.setFillColor(null);
            }

            // 设置边框
            String stroke = element.getAttribute("stroke");
            if (!stroke.isEmpty() && !stroke.equals("none")) {
                shape.setLineColor(parseColor(stroke));
                String strokeWidth = element.getAttribute("stroke-width");
                if (!strokeWidth.isEmpty()) {
                    double width = Double.parseDouble(strokeWidth);
                    shape.setLineWidth(width);
                }
            } else {
                shape.setLineWidth(0); // 无边框
            }

        } catch (Exception e) {
            System.err.println("创建路径元素时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建折线元素
     */
    private static void createPolyline(Element element, XSLFSlide slide, double translateX, double translateY) {
        try {
            String points = element.getAttribute("points");
            if (points == null || points.isEmpty()) return;

            String[] pointPairs = points.trim().split("\\s+");
            if (pointPairs.length < 2) return;

            double[] last = null;
            for (String pair : pointPairs) {
                String[] xy = pair.split(",");
                double x = Double.parseDouble(xy[0]) + translateX;
                double y = Double.parseDouble(xy[1]) + translateY;
                if (last != null) {
                    createLine(slide, last[0], last[1], x, y, element);
                }
                last = new double[]{x, y};
            }
        } catch (Exception e) {
            System.err.println("创建折线元素时发生错误: " + e.getMessage());
        }
    }

    /**
     * 解析颜色值
     */
    private static Color parseColor(String colorStr) {
        if (colorCache.containsKey(colorStr)) {
            return colorCache.get(colorStr);
        }

        Color color;
        if (colorStr.startsWith("#")) {
            // 解析16进制颜色
            String hex = colorStr.substring(1);
            if (hex.length() == 3) {
                // 将#RGB转为#RRGGBB
                hex = String.valueOf(hex.charAt(0)) + hex.charAt(0) +
                      hex.charAt(1) + hex.charAt(1) +
                      hex.charAt(2) + hex.charAt(2);
            }
            color = new Color(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16)
            );
        } else if (colorStr.startsWith("rgb")) {
            // 解析rgb(r,g,b)格式
            String[] parts = colorStr.substring(colorStr.indexOf('(') + 1, colorStr.indexOf(')')).split(",");
            color = new Color(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim())
            );
        } else {
            // 预定义颜色名称
            switch (colorStr.toLowerCase()) {
                case "black": color = Color.BLACK; break;
                case "blue": color = Color.BLUE; break;
                case "cyan": color = Color.CYAN; break;
                case "gray": color = Color.GRAY; break;
                case "green": color = Color.GREEN; break;
                case "magenta": color = Color.MAGENTA; break;
                case "orange": color = Color.ORANGE; break;
                case "pink": color = Color.PINK; break;
                case "red": color = Color.RED; break;
                case "white": color = Color.WHITE; break;
                case "yellow": color = Color.YELLOW; break;
                default: color = Color.BLACK;
            }
        }

        colorCache.put(colorStr, color);
        return color;
    }

    /**
     * 应用透明度到颜色
     */
    private static Color applyOpacity(Color color, double opacity) {
        if (color == null) return null;
        int alpha = (int) (opacity * 255);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * 解析SVG属性中的数值
     */
    private static double parseDoubleAttribute(Element element, String attrName, double defaultValue) {
        String attr = element.getAttribute(attrName);
        if (attr == null || attr.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(attr.replaceAll("px", "").trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 计算文本宽度
     * 中文字符宽度 = 字体大小
     * 英文和数字宽度 = 字体大小/2
     */
    private static double calculateTextWidth(String text, double fontSize) {
        if (text == null || text.isEmpty()) {
            return fontSize * 2; // 最小宽度
        }

        double width = 0;
        for (char c : text.toCharArray()) {
            width += fontSize;
//            if (isChinese(c)) {
//                width += fontSize;
//            } else {
//                width += fontSize / 2;
//            }
        }
        width += fontSize;
        if (fontSize < 15) {
            width += fontSize;
        }
        return Math.max(width, fontSize * 2); // 确保最小宽度
    }

    /**
     * 判断字符是否为中文
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    /**
     * 新增：纯Java实现SVG弧线转贝塞尔
     */
    private static void arcToBezierPureJava(java.awt.geom.Path2D.Double path, double x0, double y0, double x, double y,
                                double rx, double ry, double angle, int largeArcFlag, int sweepFlag) {
        // 参考 https://github.com/fontello/svgpath/blob/master/lib/a2c.js
        // 这里只实现最常见的饼图扇形（大部分场景足够）
        // 1. 旋转坐标系
        double sinPhi = Math.sin(angle);
        double cosPhi = Math.cos(angle);
        // 2. 计算 (x1', y1')
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        double x1p = cosPhi * dx2 + sinPhi * dy2;
        double y1p = -sinPhi * dx2 + cosPhi * dy2;
        // 3. 修正半径
        rx = Math.abs(rx); ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1p = x1p * x1p;
        double Py1p = y1p * y1p;
        double radicant = (Prx * Pry - Prx * Py1p - Pry * Px1p) / (Prx * Py1p + Pry * Px1p);
        radicant = Math.max(0, radicant);
        double coef = (largeArcFlag != sweepFlag ? 1 : -1) * Math.sqrt(radicant);
        double cxp = coef * (rx * y1p) / ry;
        double cyp = coef * -(ry * x1p) / rx;
        // 4. 计算中心 (cx, cy)
        double cx = cosPhi * cxp - sinPhi * cyp + (x0 + x) / 2.0;
        double cy = sinPhi * cxp + cosPhi * cyp + (y0 + y) / 2.0;
        // 5. 计算起止角度
        double v1x = (x1p - cxp) / rx;
        double v1y = (y1p - cyp) / ry;
        double v2x = (-x1p - cxp) / rx;
        double v2y = (-y1p - cyp) / ry;
        double theta1 = Math.atan2(v1y, v1x);
        double deltaTheta = Math.atan2(v2y, v2x) - theta1;
        if (sweepFlag == 1 && deltaTheta < 0) deltaTheta += 2 * Math.PI;
        if (sweepFlag == 0 && deltaTheta > 0) deltaTheta -= 2 * Math.PI;
        // 6. 拆分为若干贝塞尔段
        int segments = (int)Math.ceil(Math.abs(deltaTheta / (Math.PI / 2)));
        double delta = deltaTheta / segments;
        double t = 8.0 / 3.0 * Math.sin(delta / 4.0) * Math.sin(delta / 4.0) / Math.sin(delta / 2.0);
        double startAngle = theta1;
        double px = x0, py = y0;
        for (int i = 0; i < segments; i++) {
            double a1 = startAngle + i * delta;
            double a2 = a1 + delta;
            double cosA1 = Math.cos(a1), sinA1 = Math.sin(a1);
            double cosA2 = Math.cos(a2), sinA2 = Math.sin(a2);
            // 控制点1
            double x1 = px + t * (-rx * sinA1 * cosPhi - ry * cosA1 * sinPhi);
            double y1 = py + t * (-rx * sinA1 * sinPhi + ry * cosA1 * cosPhi);
            // 控制点2
            double x2 = (cx + rx * cosA2 * cosPhi - ry * sinA2 * sinPhi)
                      + t * (rx * sinA2 * cosPhi + ry * cosA2 * sinPhi);
            double y2 = (cy + rx * cosA2 * sinPhi + ry * sinA2 * cosPhi)
                      + t * (rx * sinA2 * sinPhi - ry * cosA2 * cosPhi);
            // 终点
            double ex = cx + rx * cosA2 * cosPhi - ry * sinA2 * sinPhi;
            double ey = cy + rx * cosA2 * sinPhi + ry * sinA2 * cosPhi;
            path.curveTo(x1, y1, x2, y2, ex, ey);
            px = ex; py = ey;
        }
    }

    /**
     * 创建线条
     */
    private static void createLine(XSLFSlide slide, double x1, double y1, double x2, double y2, Element element) {
        XSLFConnectorShape line = slide.createConnector();
        line.setAnchor(new Rectangle2D.Double(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.abs(x2 - x1),
                Math.abs(y2 - y1)
        ));

        // 设置线条颜色
        String stroke = element.getAttribute("stroke");
        if (!stroke.isEmpty()) {
            line.setLineColor(parseColor(stroke));
        }

        // 设置线条宽度
        String strokeWidth = element.getAttribute("stroke-width");
        if (!strokeWidth.isEmpty()) {
            double width = Double.parseDouble(strokeWidth);
            line.setLineWidth(width);
        }
    }
} 