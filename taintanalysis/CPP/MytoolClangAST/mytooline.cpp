#include "clang/AST/AST.h"
#include "clang/AST/ASTConsumer.h"
#include "clang/AST/RecursiveASTVisitor.h"
#include "clang/Basic/SourceManager.h"
#include "clang/Frontend/CompilerInstance.h"
#include "clang/Frontend/FrontendActions.h"
#include "clang/Tooling/Tooling.h"
#include <iostream>

using namespace clang;
using namespace clang::tooling;

class IfStmtVisitor : public RecursiveASTVisitor<IfStmtVisitor> {
private:
    SourceManager &SM;
    std::string functionName;
    std::string targetVariableName;

public:
    IfStmtVisitor(SourceManager &SM, std::string funcName, std::string targetVar)
        : SM(SM), functionName(funcName), targetVariableName(targetVar) {}

    bool VisitFunctionDecl(FunctionDecl *func) {
        if (func->getNameInfo().getName().getAsString() == functionName) {
            TraverseStmt(func->getBody());
        }
        return true;
    }

    bool VisitIfStmt(IfStmt *stmt) {
        Expr *condition = stmt->getCond();
        if (condition) {
            if (condition->getStmtClassName() == "BinaryOperator") {
                BinaryOperator *binaryOp = cast<BinaryOperator>(condition);
                if (binaryOp->isAssignmentOp()) {
                    Expr *lhs = binaryOp->getLHS();
                    if (isa<DeclRefExpr>(lhs)) {
                        DeclRefExpr *declRef = cast<DeclRefExpr>(lhs);
                        if (declRef->getNameInfo().getAsString() == targetVariableName) {
                            FullSourceLoc loc = SM.getSpellingLoc(stmt->getBeginLoc());
                            if (!loc.isInSystemHeader() && !loc.isInExternCSystemHeader()) {
                                llvm::outs() << "Found target variable '" << targetVariableName << "' in if statement at line "
                                             << loc.getLineNumber() << "\n";
                            }
                        }
                    }
                }
            }
        }
        return true; // Continue traversal
    }
};

class IfStmtASTConsumer : public ASTConsumer {
private:
    CompilerInstance &CI;
    std::string functionName;
    std::string targetVariableName;

public:
    IfStmtASTConsumer(CompilerInstance &CI, std::string funcName, std::string targetVar)
        : CI(CI), functionName(funcName), targetVariableName(targetVar) {}

    void HandleTranslationUnit(ASTContext &context) override {
        IfStmtVisitor visitor(CI.getSourceManager(), functionName, targetVariableName);
        visitor.TraverseDecl(context.getTranslationUnitDecl());
    }
};

class IfStmtFrontendAction : public ASTFrontendAction {
private:
    std::string functionName;
    std::string targetVariableName;

public:
    IfStmtFrontendAction(std::string funcName, std::string targetVar)
        : functionName(funcName), targetVariableName(targetVar) {}

    std::unique_ptr<ASTConsumer> CreateASTConsumer(CompilerInstance &CI, llvm::StringRef file) override {
        return std::make_unique<IfStmtASTConsumer>(CI, functionName, targetVariableName);
    }
};

int main(int argc, const char **argv) {
    if (argc < 4) {
        llvm::errs() << "Usage: ifstmt_search <source_file> <function_name> <target_variable>\n";
        return 1;
    }

    std::string sourceFile = argv[1];
    std::string functionName = argv[2];
    std::string targetVariable = argv[3];

    CommonOptionsParser optionsParser(argc, argv);
    ClangTool tool(optionsParser.getCompilations(), optionsParser.getSourcePathList());

    return tool.run(newFrontendActionFactory<IfStmtFrontendAction>(functionName, targetVariable).get());
}