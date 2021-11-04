import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String CSV_FILE_PATH = "data.csv";
    private static final String XML_FILE_PATH = "data.xml";
    private static final String JSON_FILE_PATH = "new_data.json";
    private static final String JSON_FROM_CSV_FILE_PATH = "data.json";
    private static final String JSON_FROM_XML_FILE_PATH = "data2.json";

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> list = parseCSV(columnMapping, CSV_FILE_PATH);
        List<Employee> list2 = parseXML(XML_FILE_PATH);
        String inputJson = readString(JSON_FILE_PATH);

        String json = listToJson(list);
        String json2 = listToJson(list2);
        List<Employee> employees = jsonToList(inputJson);

        writeString(json, JSON_FROM_CSV_FILE_PATH);
        writeString(json2, JSON_FROM_XML_FILE_PATH);
        showEmployees(employees);
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> employees = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();

            employees = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public static List<Employee> parseXML(String fileName) {
        ArrayList<Employee> employees = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node_ = nodeList.item(i);
                if (Node.ELEMENT_NODE == node_.getNodeType()) {
                    employees.add(getEmployeeFromElement(node_));
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            System.out.println(ex.getMessage());
        }
        return employees;
    }

    public static Employee getEmployeeFromElement(Node element) {
        ArrayList employeeValues = new ArrayList();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);
            if (Node.ELEMENT_NODE == node_.getNodeType()) {
                Element el = (Element) node_;
                employeeValues.add(el.getTextContent());
            }
        }
        Long id = Long.parseLong(employeeValues.get(0).toString());
        String name = employeeValues.get(1).toString();
        String surname = employeeValues.get(2).toString();
        String country = employeeValues.get(3).toString();
        int age = Integer.parseInt(employeeValues.get(4).toString());
        return new Employee(id, name, surname, country, age);
    }

    public static String listToJson(List<Employee> employees) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(employees, listType);

    }

    public static void writeString(String json, String path) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(json);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static String readString(String path) {
        String s;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return sb.toString();
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> employees = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONArray list = (JSONArray) parser.parse(json);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            for (Object el : list) {
                Employee employee = gson.fromJson(el.toString(), Employee.class);
                employees.add(employee);
            }
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
        return employees;
    }

    public static void showEmployees(List<Employee> employees) {
        employees.stream()
                .forEach(System.out::println);
    }
}