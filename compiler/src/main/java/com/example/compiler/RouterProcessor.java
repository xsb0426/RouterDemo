package com.example.compiler;

import com.example.annotation.Route;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * @author xushibin
 * @date 2019-10-28
 * description：
 */
@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {

    /**
     * 生成文件的包名
     */
    private static final String GENERATE_PACKAGE = "com.example.genarate";

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    /**
     * 需要扫描的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new HashSet<>();
        supportedAnnotationTypes.add(Route.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    /**
     * 支持的jdk版本
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
            Map<String, String> map = new HashMap<>();
            String group = "";
            for (Element element : elements) {
                TypeElement typeElement = (TypeElement) element;
                Route annotation = typeElement.getAnnotation(Route.class);
                String value = annotation.value();
                map.put(value, typeElement.getQualifiedName().toString());
                //router path 必须是/ggg/xxx格式，其中ggg就是group，一般为模块名
                group = value.substring(value.indexOf("/") + 1, value.indexOf("/", 1));
            }
            if (map.size() > 0) {
                writeToFile(map, group);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 生成文件
     *
     * @param map
     * @param group
     * @throws IOException
     */
    private void writeToFile(Map<String, String> map, String group) throws IOException {
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Class.class)
        );
        ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedTypeName, "routerMap").build();

        //生成方法描述
        MethodSpec.Builder builder = MethodSpec.methodBuilder("loadRouterMap")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(parameterSpec);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            ClassName className = ClassName.get(GENERATE_PACKAGE, entry.getValue());
            builder.addStatement("routerMap.put($S, $T.class)", entry.getKey(), className);
        }
        MethodSpec loadRouterMap = builder.build();

        ClassName iRouter = ClassName.get("com.example.router", "IRouter");
        //生成类描述
        TypeSpec typeSpec = TypeSpec.classBuilder("Router$$" + group)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(loadRouterMap)
                .addSuperinterface(iRouter)
                .build();
        JavaFile javaFile = JavaFile.builder(GENERATE_PACKAGE, typeSpec)
                .build();
        javaFile.writeTo(filer);
    }

}
