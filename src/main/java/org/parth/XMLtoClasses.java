package org.parth;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XMLtoClasses {


    public static void getClassfromBean(String xmlFile, ArrayList<String> beans)
    {

        //getting classes form the beans
        try {
            File inputFile = new File(xmlFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            XPath xPath =  XPathFactory.newInstance().newXPath();

            String expression = "/beans/bean";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
                    doc, XPathConstants.NODESET);

            String byType="class";
            String byName="id";

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
//                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String str = eElement.getAttribute(byType);
//                    System.out.println("Class is : "+ str);
                    beans.add(str);

                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }


        return;
    }

    public static void getClassfromPackage(String xmlFile, ArrayList<String> beans) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        ArrayList<String> allPackages= new ArrayList<>();

        File inputFile = new File(xmlFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        XPath xPath =  XPathFactory.newInstance().newXPath();

        String expression = "/beans/component-scan";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
                doc, XPathConstants.NODESET);

//            System.out.println(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node nNode = nodeList.item(i);
//                System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element eElement = (Element) nNode;
                String packagesList = eElement.getAttribute("base-package");
//                    System.out.println("package is : "+ packagesList);

                // add all the packages into an array list
                List<String> myList = new ArrayList<String>(Arrays.asList(packagesList.split(",")));
                for(String tmp:myList)
                {
//                        System.out.println(tmp);
                    allPackages.add(tmp);
                }

            }

        }

        // add classes of each package into the beans list
        for(String currPackageName:allPackages)
        {
            List<Class<?>> classes = ClassFinder.find(currPackageName);
            Iterator<Class<?>> itr = classes.iterator();
            while (itr.hasNext()) {
//            System.out.println(itr.next());
                String className= itr.next().toString();
                String classNametmp=removeClassPrefix(className);

                beans.add(classNametmp);
            }
        }

        return;
    }

    public static String removeClassPrefix(String className) // remove the prefix :"class "
    {
        String classNametmp="";
        int n=className.length();
        for(int i=6;i<n;i++)
        {
            classNametmp+=className.charAt(i);
        }
        return  classNametmp;
    }
    public static void removeClassprefix_forList(ArrayList<String> beans)
    {
        for (int i = 0; i < beans.size(); i++)
        {
            String ss = beans.get(i);
            ss = removeClassPrefix(ss);
            beans.set(i, ss);
        }
    }

    public static void getAllClassesfromXml(String xmlFile,ArrayList<String> beans) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        getClassfromBean(xmlFile,beans);
        getClassfromPackage(xmlFile,beans);

        return;
    }
}
