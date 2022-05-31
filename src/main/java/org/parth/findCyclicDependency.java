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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class findCyclicDependency {
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
                    String packages = eElement.getAttribute("base-package");
//                    System.out.println("package is : "+ packages);

                    List<String> myList = new ArrayList<String>(Arrays.asList(packages.split(",")));
                    for(String tmp:myList)
                    {
//                        System.out.println(tmp);
                        allPackages.add(tmp);
                    }

                }

            }

            // add all classes of each package into the beans list
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

    public static String removeClassPrefix(String className)
    {
        String classNametmp="";
        int n=className.length();
        for(int i=6;i<n;i++)
        {
            classNametmp+=className.charAt(i);
        }
       return  classNametmp;
    }

    public static void getAllClassesfromXml(String xmlFile,ArrayList<String> beans) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        getClassfromBean(xmlFile,beans);
        getClassfromPackage(xmlFile,beans);

        return;
    }

    public static void getusefulClasses(ArrayList<String> allClasses,ArrayList<String> beans) throws ClassNotFoundException {
        
        for(String className:allClasses)
        {
            Class<?> thisClass = Class.forName(className);
            if( thisClass.isAnnotationPresent(Component.class)!=false) {
                beans.add(thisClass.toString());
            }
            else if (thisClass.isAnnotationPresent(Service.class)!=false || thisClass.isAnnotationPresent(Repository.class)!=false || thisClass.isAnnotationPresent(Controller.class)!=false) {
                beans.add(thisClass.toString());
            }

        }

        return;
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {

        ArrayList<String> allClasses = new ArrayList<>();

        // get classes from bean-id pairs and the packages to be scanned
        getAllClassesfromXml("ApplicationContext.xml",allClasses);


//        for(String s:allClasses) { System.out.println(s); }

        ArrayList<String> beans = new ArrayList<>();
        getusefulClasses(allClasses,beans);// with @component annotation


        //remove prefix "class" from all usefull classes
        for (int i=0;i< beans.size();i++)
        {
            String ss=beans.get(i);
            ss=removeClassPrefix(ss);
            beans.set(i,ss);
        }

//        System.out.println("\n usefull classes are : ");
//        for(String s:beans) {System.out.println(s);}

        edges= new HashMap<>();

        makeDirectedGraph(beans);

        printGraph();
        boolean cycle=findCycle();
        System.out.println("the graph has a cycle : "+ cycle);


    }


    private static void makeDirectedGraph(ArrayList<String> beans) throws ClassNotFoundException {

        for(String className:beans){
//            System.out.println("\n"+className);

            Class<?> thisClass = Class.forName(className);    // should not have prefix "class"


            try {

                Field[] fields=thisClass.getDeclaredFields();       // declared se private bhi aa jaaenge but inherited nahi aenge
                Method[] methods = thisClass.getDeclaredMethods();      // inherited nahi aenge
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
//                        System.out.println("field "+i+" is of type "+currField);
//                        System.out.println("this is annotated with "+fields[i].getDeclaredAnnotation(Autowired.class));

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

    }

    private static void printGraph()
    {
        System.out.println("graph looks like : ");
        System.out.println(Arrays.deepToString(edges.entrySet().toArray()));
    }
}
// qualifier
// beans/bean

// commit