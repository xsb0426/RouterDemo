package com.example.compiler;

import com.example.annotation.Autowired;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author xushibin
 * @date 2019-11-19
 * description：注解处理器 扫描Autowired注解
 */
@AutoService(Processor.class)
public class AutowiredProcessor extends AbstractProcessor {

    // Java type
    private static final String LANG = "java.lang";
    public static final String BYTE = LANG + ".Byte";
    public static final String SHORT = LANG + ".Short";
    public static final String INTEGER = LANG + ".Integer";
    public static final String LONG = LANG + ".Long";
    public static final String FLOAT = LANG + ".Float";
    public static final String DOUBEL = LANG + ".Double";
    public static final String BOOLEAN = LANG + ".Boolean";
    public static final String CHAR = LANG + ".Character";
    public static final String STRING = LANG + ".String";
    public static final String SERIALIZABLE = "java.io.Serializable";

    private static final ClassName RouterClass = ClassName.get("com.example.router", "Router");

    //类和对象的对应关系，一个类可能扫描到多个注解，用map保存
    private Map<TypeElement, List<Element>> typeAndField = new HashMap<>();

    private Filer filer;
    private Types types;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new HashSet<>();
        supportedAnnotationTypes.add(Autowired.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (annotations != null && annotations.size() > 0) {
            //注解集合
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Autowired.class);
            if (elements != null && elements.size() > 0) {
                for (Element element : elements) {
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                    if (element.getModifiers().contains(Modifier.PRIVATE)) {
                        throw new IllegalStateException("注入的成员变量不能是私有的!!! 请检查 ["
                                + element.getSimpleName() + "] 在类 [" + enclosingElement.getQualifiedName() + "]");
                    }
                    //组装类和成员变量到map中，一个类可能对应多个注解 Map<TypeElement, List<Element>>
                    if (typeAndField.containsKey(enclosingElement)) {
                        typeAndField.get(enclosingElement).add(element);
                    } else {
                        List<Element> childs = new ArrayList<>();
                        childs.add(element);
                        typeAndField.put(enclosingElement, childs);
                    }
                }

                try {
                    writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        return false;
    }

    private void writeToFile() throws IOException {
        //获取 IProvider 的类型信息
        TypeMirror iProvider = elementUtils.getTypeElement("com.example.router.IProvider").asType();
        //生成类
        for (Map.Entry<TypeElement, List<Element>> entry : typeAndField.entrySet()) {
            List<Element> elements = entry.getValue();
            TypeElement typeElement = entry.getKey();
            String qualifiedName = typeElement.getQualifiedName().toString();
            String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));

            //构建方法
            ClassName className = ClassName.get(typeElement);
            MethodSpec.Builder builder = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(Object.class, "object")
                    .addStatement("$T activity = ($T) object", className, className);
            for (Element element : elements) {
                Autowired annotation = element.getAnnotation(Autowired.class);
                String value = annotation.value();
                String fieldName = element.getSimpleName().toString();
                //如果注解没有value,就使用fieldName
                if ("".equals(value)) {
                    value = fieldName;
                }
                TypeMirror typeMirror = element.asType();
                // 判断不是Element类型本身
                if (!typeMirror.getKind().isPrimitive()) {
                    String type = typeMirror.toString();
                    //根据不同类型，生成对应的代码
                    if (STRING.equals(type)) {
                        builder.addStatement("activity.$N = activity.getIntent().getStringExtra($S)", fieldName, value);
                    } else if (INTEGER.equals(type)) {
                        builder.addStatement("activity.$N = activity.getIntent().getIntExtra($S, 0)", fieldName, value);
                    } else if (BOOLEAN.equals(type)) {
                        builder.addStatement("activity.$N = activity.getIntent().getBooleanExtra($S, false)", fieldName, value);
                    } else {
                        //如果被注解变量的类型是IProvider的子类
                        if (types.isSubtype(typeMirror, iProvider)) {
                            builder.addStatement("Class iProvider = $T.getInstance().getRouterMap().get($S)", RouterClass, value);
                            builder.beginControlFlow("if(iProvider != null)");
                            builder.beginControlFlow("try");
                            builder.addStatement("activity.$N = ($N) iProvider.newInstance()", fieldName, type);
                            builder.nextControlFlow("catch ($T e)", Exception.class);
                            builder.addStatement("e.printStackTrace()");
                            builder.endControlFlow();
                            builder.endControlFlow();
                        }
                    }
                }

            }
            MethodSpec methodSpec = builder.build();

            //构建类
            ClassName iSyringe = ClassName.get("com.example.router", "ISyringe");
            TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$Autowired")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(methodSpec)
                    .addSuperinterface(iSyringe)
                    .build();

            //生成文件
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();
            javaFile.writeTo(filer);
        }
    }
}
