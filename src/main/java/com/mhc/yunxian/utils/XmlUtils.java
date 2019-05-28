package com.mhc.yunxian.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML工具类.
 *
 * @author zhayido
 * @version 1.0 18/4/23
 * @since 1.0
 */
public class XmlUtils {

    /**
     * 将XML字符串转为Map.
     *
     * @param xml
     * @return
     */
    public static Map<String, String> parse2Map(String xml) {
        if (StringUtils.isBlank(xml)) {
            return new HashMap<>();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(IOUtils.toInputStream(xml));
            NodeList allNodes = document.getFirstChild().getChildNodes();

            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < allNodes.getLength(); i++) {
                Node node = allNodes.item(i);
                if (node instanceof Element) {
                    map.put(node.getNodeName(), node.getTextContent());
                }
            }

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Map<String,String> inputStream2Map(InputStream inputStream){
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();
        // 读取输入流
        SAXReader reader = new SAXReader();
        org.dom4j.Document document = null;
        try {
            document = reader.read(inputStream);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        // 得到xml根元素
        org.dom4j.Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<org.dom4j.Element> elementList = root.elements();

        // 遍历所有子节点
        for (org.dom4j.Element e : elementList){
            map.put(e.getName(), e.getText());
        }
        return map;
    }
}
