package nl.capaxit.tooltiprunner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by jamiecraane on 06/07/16.
 */
public class TooltipRunnerRunConfiguration extends ApplicationConfiguration {
    protected TooltipRunnerRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(name, project, factory);
    }

    @Override
    public Icon getIcon() {
        return super.getIcon();
    }

    @Override
    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        final JavaCommandLineState state = new JavaApplicationCommandLineState<ApplicationConfiguration>(this, env);
        final Editor editor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        state.setConsoleBuilder(new MyConsoleViewBuilder(getProject(), editor, env.getExecutor().getToolWindowId()));
        return state;
    }

    public class MyConsoleViewBuilder extends TextConsoleBuilder {
        private final Project project;
        private final Editor editor;
        private final String toolWindowId;
        private StringBuilder sb;
        private boolean firstTime = true;

        public MyConsoleViewBuilder(Project project, Editor editor, String toolWindowId) {
            this.project = project;
            this.editor = editor;
            this.toolWindowId = toolWindowId;
        }

        @Override
        public ConsoleView getConsole() {
            return new ConsoleViewImpl(project, true) {
                @Override
                public void print(@NotNull String s, @NotNull ConsoleViewContentType contentType) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        ToolWindowManager.getInstance(getProject()).getToolWindow(toolWindowId).hide(null);

                        if (firstTime) {
                            sb = new StringBuilder();
                        }

                        if (!firstTime && !endProcessOutput(s)) {
                            sb.append(s);
                        }

                        if (endProcessOutput(s)) {
                            new TooltipExecutionResultPanel(project, sb.toString(), editor);
                        }

                        firstTime = false;
                    });
                }
            };
        }

//        todo there must be a better way to determine if the process is started or terminated.
        private boolean endProcessOutput(@NotNull String s) {
            return s.contains("Process finished");
        }

        @Override
        public void addFilter(Filter filter) {

        }

        @Override
        public void setViewer(boolean isViewer) {

        }
    }
}
