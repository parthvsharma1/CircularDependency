package org.parth;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class getClassFromBeans {
    public static Map<String,ArrayList<String>> edges;

    public static  boolean isCyclicUtil(String i,Set<String> visited,Set<String> recStack)
    {
        if(recStack.contains(i)==true)
            return true;
        if(visited.contains(i)==true)
            return false;

        recStack.add(i);
        visited.add(i);
        ArrayList<String> children =edges.get(i);
        if(children==null)
            return false;
        for(String child:children)
        {
            if(isCyclicUtil(child,visited,recStack)==true)
                return true;
        }

        recStack.remove(i);
        return false;
    }
    public static boolean findCycle()
    {

        Set<String> visited = new HashSet<String>();
        Set<String> recStack = new HashSet<String>();

        for(String s:edges.keySet())
        {
            if (isCyclicUtil(s, visited, recStack))
                return true;
        }
        return  false;
    }
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArrayList<String> beans = new ArrayList<String>();

        //getting classes form the beans
        try {
            File inputFile = new File("ApplicationContext.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            XPath xPath =  XPathFactory.newInstance().newXPath();

            String expression = "/beans/bean";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
                    doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
//                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String str = eElement.getAttribute("class");
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





        edges=new HashMap<String,ArrayList<String>>();
        // adding relevant edges to the graph

        for(String className:beans){
            System.out.println();
            System.out.println(className);

            Class<?> thisClass = Class.forName(className);


            try {

                Field[] fields=thisClass.getDeclaredFields();// declared se private bhi aa jaaenge but inherited nahi aenge
                Method[] methods = thisClass.getDeclaredMethods();// inherited nahi aenge
                Constructor[] constructors= thisClass.getConstructors();


                // find auto wires in fields (works with reqd=false as well)
                for(int i=0;i<fields.length;i++){
                    if(fields[i].isAnnotationPresent(Autowired.class)!=false){


                        String updateClassName="class "+className;
                        String newnbr=fields[i].getType().toString();
                        ArrayList<String> currnbrs=edges.get(updateClassName);

                        if(currnbrs!=null) {
                            currnbrs.add(newnbr);
                            edges.put(updateClassName, currnbrs);
                        }
                        else {
                            ArrayList<String> currnbr = new ArrayList<>();
                            currnbr.add(newnbr);
                            edges.put(updateClassName,currnbr);
                        }

                    String currField=fields[i].toString();
                    System.out.println("field "+i+" is of type "+currField);
                        System.out.println("this is annotated with "+fields[i].getDeclaredAnnotation(Autowired.class));

                    }

                }
  /*
                // find auto wires in methods( mainly setters() )
                for (int i = 0; i < methods.length; i++) {
                    System.out.println("method "+i+" is "+ methods[i].toString());
//                System.out.println("annotated with "+methods[i].getDeclaredAnnotations());

                }



//              finding autowires in constructors
                for(int i=0;i< constructors.length;i++)
                {
                    System.out.println(constructors[i].toString());
                }

*/

            } catch (Throwable e) {
                System.err.println(e);
            }


        }

        System.out.println("graph looks like : ");
        System.out.println(Arrays.deepToString(edges.entrySet().toArray()));

        boolean cycle=findCycle();
        System.out.println("the graph has a cycle : "+ cycle);


    }
}
