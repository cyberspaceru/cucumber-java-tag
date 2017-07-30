package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import ru.sbtqa.plugins.cucumber.util.TagProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CucumberJavaExtension extends AbstractCucumberJavaExtension {
    public static final String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";

    @NotNull
    @Override
    public BDDFrameworkType getStepFileType() {
        return new BDDFrameworkType(JavaFileType.INSTANCE);
    }

    @NotNull
    @Override
    public StepDefinitionCreator getStepDefinitionCreator() {
        return new JavaStepDefinitionCreator();
    }

    @Override
    public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
        final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

        final List<AbstractStepDefinition> result = new ArrayList<>();
        result.addAll(loadAnnotatedBy(CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION, dependenciesScope, module));
        result.addAll(loadAnnotatedBy(TagProject.PAGE_ENTRY_ANNOTATION_QUALIFIED_NAME, dependenciesScope, module));
        result.addAll(loadAnnotatedBy(TagProject.ACTION_TITLE_ANNOTATION_QUALIFIED_NAME, dependenciesScope, module));
        return result;
    }

    private List<AbstractStepDefinition> loadAnnotatedBy(String annotationQualifiedName, GlobalSearchScope dependenciesScope, Module module) {
        PsiClass psiAnnotationClass = JavaPsiFacade.getInstance(module.getProject()).findClass(annotationQualifiedName, dependenciesScope);
        if (psiAnnotationClass == null)
            return Collections.emptyList();

        final List<AbstractStepDefinition> result = new ArrayList<>();
        final Query<PsiClass> defAnnotations = AnnotatedElementsSearch.searchPsiClasses(psiAnnotationClass, dependenciesScope);
        for (PsiClass annotationClassInstance : defAnnotations) {
            if (annotationClassInstance.isAnnotationType())
                AnnotatedElementsSearch.searchPsiMethods(annotationClassInstance, dependenciesScope).findAll().forEach(x -> result.add(new JavaStepDefinition(x)));
            else if (!annotationQualifiedName.equals(CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION))
                result.add(new JavaStepDefinition(annotationClassInstance));
        }
        return result;
    }

}
