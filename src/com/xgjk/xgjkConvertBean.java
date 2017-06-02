package com.xgjk;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.compiler.CompilationException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by XG on 2017/6/1.
 */
public class xgjkConvertBean extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        init(e);
    }

    /**
     * 初始化处理
     * @param e
     */
    private void init(AnActionEvent e) {
        //根据响应的事件 获取到当前事件所在的项目、编辑器、文件、
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        assert editor != null;
        Document document = editor.getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();
        //根据编辑器获取当前的model、获取选中的文本
        String modelSelectedText = selectionModel.getSelectedText();
        //校验选中的文本
        if (StringUtil.isEmpty(modelSelectedText)) {
            return;
        }
        //获取当前上下文的类
        PsiClass mPsiClass = getPsiClassFromContext(e,psiFile,editor);
        //创建一个对话框、提示消息、获取输入的消息
        String aimName = Messages.showInputDialog(project, "目的模型的名称？", "请输入名称", Messages.getQuestionIcon());
        //通过某个字段名字拿到全项目中的该类对象
        assert project != null;
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).
                getClassesByName(modelSelectedText, GlobalSearchScope.allScope(project));
        // 得到convertBean
        StringBuffer stringBuffer = getConvertBean(aimName, classesByName);
        //写入到编辑器内容
        writeEditorStr(project, editor, document, selectionModel, stringBuffer);

    }

    /**
     * 得到convertBean
     * @param aimName
     * @param classesByName
     * @return
     */
    @NotNull
    private StringBuffer getConvertBean(String aimName, PsiClass[] classesByName) {
        //获取到 转换的 bean
        StringBuffer stringBuffer = new StringBuffer();
        for (PsiClass psiClass : classesByName) {//处理多个相同的bean
            PsiField[] allFields = psiClass.getAllFields();//所有的类
            String name = psiClass.getName();//获取类的名字
            assert name != null;
            //concat 连接字符 没有StringBuffer好效率上
            String concatStart = name.concat(" ").concat(getLowOrUpStr(name,false)).concat("=").concat("new ").concat(name).concat("();");
            //创建 new 类
            stringBuffer.append("\t\t").append(concatStart).append("\n");
            for (PsiField allField : allFields) {
                //获取类中的所有修饰符
                PsiModifierList modifierList = allField.getModifierList();
                assert modifierList != null;
                //是否是静态的字段
                boolean isStatic = modifierList.toString().contains("static");
                if (isStatic) {
                    continue;
                }
                //构造set方法
                String comUpStr = getLowOrUpStr(allField.getName(),true);
                assert aimName != null;
                String concatStr = getLowOrUpStr(name,false).concat(".set").concat(comUpStr).concat("(")
                        .concat(aimName).concat(".get").concat(comUpStr)
                        .concat("()").concat(");");
                stringBuffer.append("\t\t").append(concatStr).append("\n");
            }
            //换行
            stringBuffer.append("\n");
        }
        return stringBuffer;
    }

    /**
     * 写入到编辑器内容
     * @param project
     * @param editor
     * @param document
     * @param selectionModel
     * @param stringBuffer
     */
    private void writeEditorStr(Project project, Editor editor, Document document, SelectionModel selectionModel, StringBuffer stringBuffer) {
        //获取偏移量
        final int offset = editor.getCaretModel().getOffset();
        int lineNumber = document.getLineNumber(offset) + 1;
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        //创建线程 输入到编译器中
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //写入
                document.insertString(lineStartOffset, stringBuffer.toString());
            }
        };
        //执行写入
        WriteCommandAction.runWriteCommandAction(project, runnable);
        //移除掉选择的model
        selectionModel.removeSelection();
    }


    /**
     * 首字母变为大写,false 小写，true 大写
     * @param str
     */
    private String getLowOrUpStr(String str,boolean isLowOrUp) {
        assert str != null;
        String substringStr = str.substring(0, 1);
        String lowStr = !isLowOrUp?substringStr.toLowerCase():substringStr.toUpperCase();
        String substringEnd = str.substring(1, str.length());
        return lowStr + substringEnd;
    }
    /**
     * 获取事件的 PsiClass
     * @param e
     * @return
     */
    private PsiClass getPsiClassFromContext(AnActionEvent e,PsiFile psiFile,Editor editor) {

        if (psiFile == null || editor == null) {
            return null;
        }
        //获取插入的model，并获取偏移量
        int offset = editor.getCaretModel().getOffset();
        //根据偏移量找到psi元素
        PsiElement element = psiFile.findElementAt(offset);
        //根据元素获取到当前的上下文的类
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    /**
     *
     * 无用的东西
     * @param document
     * @param offset
     */
    private void noUse(Document document,int offset){
        //根据偏移量得到行数
    int lineNum = document.getLineNumber(offset);
    //获取当前行的偏移量
    int startOffset = document.getLineStartOffset(lineNum);
    CharSequence editorText = document.getCharsSequence();
    //获取文本的偏移量
    int wordStartOffset = getWordStartOffset(editorText, offset);
    //得到距离编译器的距离
    final int distance = wordStartOffset - startOffset;
    String blankSpace = "";
        for (int i = 0; i < distance; i++) {
        blankSpace = blankSpace + " ";
    }
    }

    /**
     * 得到插入代码的位置
     * @param editorText
     * @param cursorOffset
     * @return
     */
    private int getWordStartOffset(CharSequence editorText, int cursorOffset) {
        if (editorText.length() == 0) return 0;
        if (cursorOffset > 0 && !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))
                && Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }

        if (Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))) {
            int start = cursorOffset;

            //定位开始位置
            while (start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                start--;
            }
            return start;

        }

        return 0;

    }
}
