// https://gist.github.com/RamonDevPrivate/3bb187ef89b2666b1b1d00232100f5ee
// Author: https://github.com/RamonDevPrivate, Version 1, CC BY-NC-SA


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

abstract class ObjectNode_425 {
    String name;
    Optional<String> value;
    String identifier;
    boolean isDotted;
    ObjectNode_425[] children;
    /**
     * @param name - custom name to uniquely identify node in dot graph 
     * @param value - values of primitive types/strings or classname; displayed inside the dot node
     * @param identifier - variable name; displayed on dot arrow
     * @param isDotted - dot node with dotted lines
     * @param children - child nodes
     */
    ObjectNode_425(String name, Optional<String> value, String identifier, boolean isDotted, ObjectNode_425... children) {
        this.name = name;
        this.value = value;
        this.identifier = identifier;
        this.children = children;
        this.isDotted = isDotted;
    }

    @Override
    public String toString() {
        String output =  "";
        if (children != null && children.length > 0) {
            for (ObjectNode_425 child : children) {
                if (child == null) continue;
                output += child.toString();
                output += this.name +  " -> " + child.name + "[label=\" "+ child.identifier + "\",style=" + (child.isDotted ? "dashed" : "solid") +"] ;\n";
            }
        }
        return output;
    }
}

class RootNode_425 extends ObjectNode_425 {
    /**
     * root / start of the graph; start point is pointing on this node.
     * @param name - custom name to uniquely identify node in dot graph 
     * @param value - values of primitive types/strings or classname; displayed inside the dot node
     * @param identifier - variable name; displayed on dot arrow
     * @param children - child nodes
     */
    RootNode_425(String name, Optional<String> value, String identifier, ObjectNode_425... children) {
        super(name, value, identifier, false, children);
    }

    @Override
    public String toString() {
        String output = "start[shape=circle,label=\"\",height=.25];\n";
        output += this.name + (value.isPresent() ? " [label=\""+ this.value.get() + "\"];\n" : " [label=\"\",shape=point,height=.25];\n");
        output += "start -> " + name + "[label=\" "+ identifier + "\"] ;\n";

        return output + super.toString();
    }
}

class ChildNode_425 extends ObjectNode_425 {
    /**
     * child nodes
     * @param name - custom name to uniquely identify node in dot graph 
     * @param value - values of primitive types/strings or classname; displayed inside the dot node
     * @param identifier - variable name; displayed on dot arrow
     * @param isDotted - dot node with dotted lines
     * @param children - child nodes
     */
    ChildNode_425(String name, Optional<String> value, String identifier, boolean isDotted, ObjectNode_425... children) {
        super(name, value, identifier, isDotted, children);
    }

    @Override
    public String toString() {
        String output = this.name + (value.isPresent() ? " [label=\""+ this.value.get() + "\",style=" + (isDotted ? "dashed" : "solid") +"];\n" : " [label=\"\",shape=point,height=.25];\n");

        return output + super.toString();
    }
}

class ArrayNode extends ObjectNode_425 {
    int length;
    /**
     * array nodes are displayed as a box and have an additional value for the array length
     * can aswell be used to display any collection or map
     * @param name - custom name to uniquely identify node in dot graph 
     * @param value - values of primitive types/strings or classname; displayed inside the dot node
     * @param identifier - variable name; displayed on dot arrow
     * @param length - array length
     * @param isDotted - dot node with dotted lines
     * @param children - child nodes
     */
    ArrayNode(String name, Optional<String> value, String identifier, int length, boolean isDotted, ObjectNode_425... children) {
        super(name, value, identifier, isDotted, children);
        this.length = length;
    }

    @Override
    public String toString() {
        String output = this.name + (value.isPresent() ? " [label=\""+ this.value.get() + "\",shape=box,style=" + (isDotted ? "dashed" : "solid") +"];\n" : " [label=\"\",shape=point,height=.25];\n");
        output += this.name + "length[label=\""+ this.length + "\"];\n";
        output += this.name + "->" + this.name + "length[label=\"length\"]\n";
        return output + super.toString();
    }
}

class NodeGenerator {
    private int nodeCounter = 0; //used to generate an unique node name
        
    // save inspected objects to prevent infinite loops in case of recursion and identify already used objects 
    private Map<Object, ObjectNode_425> inspectedObject = new HashMap<>();

    private ObjectNode_425 root;

    private boolean hideGeneratedVars, inspectSuperClasses;

    private NodeGenerator(){}

    /**
     * Inspect the object using reflections and store it in a tree structure of Nodes
     * @param objectToBeInspected - root object of the tree structure; 
     * @param identifier - variable name referencing the object 
     * @return instance of NodeGenerator
     */
    public static NodeGenerator inspect(Object objectToBeInspected, String identifier) {
        return inspect(objectToBeInspected, identifier, true, true);
    }

     /**
     * Inspect the object using reflections and store it in a tree structure of Nodes
     * @param objectToBeInspected - root object of the tree structure; 
     * @param identifier - variable name referencing the object 
     * @param inspectSuperClasses - true -> super class fields are inspected too
     * @return instance of NodeGenerator
     */
    public static NodeGenerator inspect(Object objectToBeInspected, String identifier, boolean inspectSuperClasses) {
        return inspect(objectToBeInspected, identifier, inspectSuperClasses, true);
    }

    /**
     * Inspect the object using reflections and store it in a tree structure of Nodes
     * @param objectToBeInspected - root object of the tree structure; 
     * @param identifier - variable name referencing the object 
     * @param inspectSuperClasses - true -> super class fields are inspected too
     * @param hideGeneratedVars - true -> compiler generated vars are hidden
     * @return instance of NodeGenerator
     */
    public static NodeGenerator inspect(Object objectToBeInspected, String identifier, boolean inspectSuperClasses, boolean hideGeneratedVars) {
        assert !objectToBeInspected.getClass().getPackageName().startsWith("java") : "Can't inspect Java owned objects!";
        NodeGenerator g = new NodeGenerator();
        g.hideGeneratedVars = hideGeneratedVars;
        g.inspectSuperClasses = inspectSuperClasses;
        g.root = g.objectReferenceToNodeTree(objectToBeInspected, identifier, true, false);
        return g;
    }

    /**
     * Convert Node tree into a dot graph and save it in the working directory
     * @param root - root node of the Node tree
     */
    public void toGraph() {
        String dotSource = "digraph G {\n" + root.toString() + "}";
        File dot;
        byte[] img_stream = null;
        File img;
        try {
            dot = writeDotSourceToFile(dotSource);
            if (dot != null) {
                img = File.createTempFile("graph_", ".png", new File("./"));
                Runtime rt = Runtime.getRuntime();
                String[] cmd = {"dot", "-Tpng", dot.getAbsolutePath(), "-o", img.getAbsolutePath()};
                Process p = rt.exec(cmd);
                p.waitFor();
               // dot.delete();   // Delete dot file - remove this line to view the dot file
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    public ObjectNode_425 root() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    private Field[] combineFields(Class classToBeInspected, Field[] fields) {
        Field[] classFields = classToBeInspected.getDeclaredFields();
        Field[] combinedFields = new Field[fields.length + classFields.length];
        for (int i = 0; i < combinedFields.length; i++) {
            combinedFields[i] = i < fields.length ? fields[i] : classFields[i - fields.length];
        }

        Class superclass = classToBeInspected.getSuperclass();
        if (superclass != null && inspectSuperClasses) return combineFields(superclass, combinedFields);
        return combinedFields;
    }

    private ObjectNode_425 objectReferenceToNodeTree(Object objectToBeInspected, String identifier, boolean isRoot, boolean isDotted) {
        Class classToBeInspected = objectToBeInspected.getClass();

        // reuse same node for identical objects
        if (inspectedObject.keySet().stream().anyMatch(key -> key == objectToBeInspected)) {
            return new ChildNode_425(inspectedObject.get(objectToBeInspected).name, inspectedObject.get(objectToBeInspected).value, identifier, inspectedObject.get(objectToBeInspected).isDotted);
        }

        ObjectNode_425 result = isRoot 
            ? new RootNode_425("n"+nodeCounter++, Optional.of(classToBeInspected.getSimpleName()), identifier) 
            : new ChildNode_425("n"+nodeCounter++, Optional.of(classToBeInspected.getSimpleName()), identifier, isDotted);
        
        // Identify when the same object is used
        inspectedObject.put(objectToBeInspected, result);

        Field[] fields = combineFields(classToBeInspected, new Field[0]);
        ObjectNode_425[] childs = new ObjectNode_425[fields.length];

        for(int i = 0; i < fields.length; i++) {
            if (!fields[i].getName().startsWith("$") && !fields[i].canAccess(objectToBeInspected)) 
                continue;    //ignore inaccessible fields
            if (fields[i].getName().startsWith("$") && hideGeneratedVars)
                continue;   //ignore intern vars

            try {
                Object fieldObj = fields[i].get(objectToBeInspected);
                if (fieldObj != null) {
                    // reuse same node for identical fields
                    if (inspectedObject.keySet().stream().anyMatch(key -> key == fieldObj)) {
                        childs[i] = new ChildNode_425(inspectedObject.get(fieldObj).name, inspectedObject.get(fieldObj).value, fields[i].getName(), !Arrays.asList(classToBeInspected.getDeclaredFields()).contains(fields[i]));
                        continue;
                    }

                    // special cases like array, collections and maps
                    if (fields[i].getType().isArray()) {
                        childs[i] = processArray(fieldObj, fields[i].getName(), Optional.empty(), !Arrays.asList(classToBeInspected.getDeclaredFields()).contains(fields[i]));
                        continue;
                    }
                    if (Collection.class.isAssignableFrom(fieldObj.getClass())) {
                        childs[i] = processArray(((Collection)fieldObj).toArray(), fields[i].getName(), Optional.empty(), !Arrays.asList(classToBeInspected.getDeclaredFields()).contains(fields[i]));
                        childs[i].value = Optional.of(fieldObj.getClass().getSimpleName());
                        continue;
                    }
                    if (Map.class.isAssignableFrom(fieldObj.getClass())) {
                        childs[i] = processArray(((Map)fieldObj).values().toArray(), fields[i].getName(), 
                            Optional.of(((Map)fieldObj).keySet().toArray()), !Arrays.asList(classToBeInspected.getDeclaredFields()).contains(fields[i]));
                        childs[i].value = Optional.of(fieldObj.getClass().getSimpleName());
                        continue;
                    }
                }

                // regular values / objects
                childs[i] = processTypes(fields[i].getType().getTypeName(), fieldObj, fields[i].getName(), !Arrays.asList(classToBeInspected.getDeclaredFields()).contains(fields[i]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        result.children = childs;
        return result;
    }

    private ObjectNode_425 processTypes(String typename, Object obj, String identifier, boolean isDotted) {
        // special cases for primitive types and strings
        return switch (typename) {
            case "int", "java.lang.Integer" -> new ChildNode_425("n" + nodeCounter++, Optional.of(((Integer)obj).toString()), identifier, isDotted);
            case "boolean", "java.lang.Boolean" -> new ChildNode_425("n" + nodeCounter++, Optional.of(((Boolean)obj).toString()), identifier, isDotted);
            case "float", "java.lang.Float" -> new ChildNode_425("n" + nodeCounter++, Optional.of(((Float)obj).toString()), identifier, isDotted);
            case "double", "java.lang.Double" -> new ChildNode_425("n" + nodeCounter++, Optional.of(((Double)obj).toString()), identifier, isDotted);
            case "char", "java.lang.Character" -> new ChildNode_425("n" + nodeCounter++, Optional.of(((Character)obj).toString()), identifier, isDotted);
            case "String", "java.lang.String" -> new ChildNode_425("n" + nodeCounter++, Optional.of("\\\"" + ((String)obj) + "\\\""), identifier, isDotted);
            default -> (obj != null)  // if object is null display it as point
                ? (!obj.getClass().getPackageName().startsWith("java") // recursivly travel through objects that are not part of java
                    ? objectReferenceToNodeTree(obj, identifier, false, isDotted)  
                    : new ChildNode_425("n" + nodeCounter++, Optional.of(obj.getClass().getSimpleName()), identifier, isDotted)) 
                : new ChildNode_425("n" + nodeCounter++, Optional.empty(), identifier, isDotted);
        };
    }

    private ObjectNode_425 processArray(Object obj, String identifier, Optional<Object[]> index, boolean isDotted) {
        int arrayLength = Array.getLength(obj);
        ObjectNode_425[] arrayChilds = new ObjectNode_425[arrayLength];
        for (int j = 0; j < arrayLength; j++) {
            Object element = Array.get(obj, j);
            ObjectNode_425 child = (element != null) 
                ? (element.getClass().getPackageName().startsWith("java") // recursivly travel through objects that are not part of java
                    ? processTypes(element.getClass().getTypeName(), element, index.isPresent() //display regular index or custom one for e.g. maps
                        ? index.get()[j].toString() 
                        : Integer.valueOf(j).toString(), isDotted) 
                    : objectReferenceToNodeTree(element, index.isPresent() //display regular index or custom one for e.g. maps
                        ? index.get()[j].toString() 
                        : Integer.valueOf(j).toString(), false, isDotted))
                : new ChildNode_425("n" + nodeCounter++, Optional.empty(), index.isPresent() //display regular index or custom one for e.g. maps
                        ? index.get()[j].toString() 
                        : Integer.valueOf(j).toString(), isDotted);
            arrayChilds[j] = child;
        }
        return new ArrayNode("n" + nodeCounter++, Optional.of(obj.getClass().getSimpleName()), identifier, arrayLength, isDotted, arrayChilds);
    }

    private File writeDotSourceToFile(String str) throws IOException {
        File temp = File.createTempFile("temp", ".dot", new File("./"));
        FileWriter fw = new FileWriter(temp);
        fw.write(str);
        fw.close();
        return temp;
    }
}





// jshell -R-ea


// MyObject myObject = new MyObject();


// NodeGenerator g = NodeGenerator.inspect(myObject, "myObject");
// NodeGenerator g = NodeGenerator.inspect(myObject, "myObject", true, false);


// g.toGraph(); // generate dot image
// g.root(); // generated node structure
// g.toString(); // generated dot string
