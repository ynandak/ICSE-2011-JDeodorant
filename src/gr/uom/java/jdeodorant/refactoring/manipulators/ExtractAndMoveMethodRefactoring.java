package gr.uom.java.jdeodorant.refactoring.manipulators;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ExtractAndMoveMethodRefactoring implements Refactoring {
	private IFile sourceFile;
	private IFile targetFile;
	private CompilationUnit sourceCompilationUnit;
	private CompilationUnit targetCompilationUnit;
	private TypeDeclaration sourceTypeDeclaration;
	private TypeDeclaration targetTypeDeclaration;
	private MethodDeclaration sourceMethod;
	private VariableDeclarationStatement variableDeclarationStatement;
	private VariableDeclarationFragment variableDeclarationFragment;
	private List<Statement> extractStatementList;
	//includes all variable declaration statements which are related with the extracted method
	private List<VariableDeclarationStatement> variableDeclarationStatementList;
	private UndoRefactoring undoRefactoring;
	
	public ExtractAndMoveMethodRefactoring(IFile sourceFile, IFile targetFile, CompilationUnit sourceCompilationUnit, CompilationUnit targetCompilationUnit,
			TypeDeclaration sourceTypeDeclaration, TypeDeclaration targetTypeDeclaration, MethodDeclaration sourceMethod,
			VariableDeclarationStatement variableDeclarationStatement, VariableDeclarationFragment variableDeclarationFragment,
			List<Statement> extractStatementList, List<VariableDeclarationStatement> variableDeclarationStatementList) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
		this.sourceCompilationUnit = sourceCompilationUnit;
		this.targetCompilationUnit = targetCompilationUnit;
		this.sourceTypeDeclaration = sourceTypeDeclaration;
		this.targetTypeDeclaration = targetTypeDeclaration;
		this.sourceMethod = sourceMethod;
		this.variableDeclarationStatement = variableDeclarationStatement;
		this.variableDeclarationFragment = variableDeclarationFragment;
		this.extractStatementList = extractStatementList;
		this.variableDeclarationStatementList = variableDeclarationStatementList;
		this.undoRefactoring = new UndoRefactoring();
	}

	public UndoRefactoring getUndoRefactoring() {
		return undoRefactoring;
	}

	public void apply() {
		ExtractMethodRefactoring extractMethodRefactoring = new ExtractMethodRefactoring(sourceFile, sourceTypeDeclaration, sourceMethod,
			variableDeclarationStatement, variableDeclarationFragment, extractStatementList, variableDeclarationStatementList);
		extractMethodRefactoring.apply();
		undoRefactoring = extractMethodRefactoring.getUndoRefactoring();
		IJavaElement iJavaElement = JavaCore.create(sourceFile);
        ICompilationUnit iCompilationUnit = (ICompilationUnit)iJavaElement;
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(iCompilationUnit);
        parser.setResolveBindings(true);
        CompilationUnit sourceCompilationUnit = (CompilationUnit)parser.createAST(null);
        List<AbstractTypeDeclaration> typeDeclarationList = sourceCompilationUnit.types();
        TypeDeclaration sourceTypeDeclaration = null;
        MethodDeclaration extractedMethodDeclaration = null;
        for(AbstractTypeDeclaration abstractTypeDeclaration : typeDeclarationList) {
        	if(abstractTypeDeclaration instanceof TypeDeclaration) {
        		TypeDeclaration typeDeclaration = (TypeDeclaration)abstractTypeDeclaration;
        		if(typeDeclaration.resolveBinding().getQualifiedName().equals(this.sourceTypeDeclaration.resolveBinding().getQualifiedName())) {
        			sourceTypeDeclaration = typeDeclaration;
        			MethodDeclaration[] sourceClassMethodDeclarations = sourceTypeDeclaration.getMethods();
        			extractedMethodDeclaration = sourceClassMethodDeclarations[sourceClassMethodDeclarations.length-1];
        		}
        	}
        }
		MoveMethodRefactoring moveMethodRefactoring = new MoveMethodRefactoring(sourceFile, targetFile, sourceCompilationUnit, targetCompilationUnit,
			sourceTypeDeclaration, targetTypeDeclaration, extractedMethodDeclaration, false);
		moveMethodRefactoring.apply();
		UndoRefactoring moveMethodUndoRefactoring = moveMethodRefactoring.getUndoRefactoring();
		undoRefactoring.merge(moveMethodUndoRefactoring);
	}
}