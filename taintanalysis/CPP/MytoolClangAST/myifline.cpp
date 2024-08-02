#include "clang/AST/ASTConsumer.h"
#include "clang/AST/RecursiveASTVisitor.h"
#include "clang/AST/ASTContext.h"
#include "clang/Frontend/CompilerInstance.h"
#include "clang/Frontend/FrontendAction.h"
#include "clang/Frontend/ASTConsumers.h"
#include "clang/Tooling/CommonOptionsParser.h"
#include "clang/Tooling/Tooling.h"

#include <iostream>
#include <string>
#include <vector>

using namespace clang;
using namespace clang::tooling;
using namespace llvm;

static cl::OptionCategory MyToolCategory("my-tool options");

class MyASTVisitor : public RecursiveASTVisitor<MyASTVisitor> {
public:
    bool VisitIfStmt(IfStmt *stmt) {    
        SourceLocation startLoc = stmt->getBeginLoc();
        SourceLocation endLoc = stmt->getEndLoc();
        // 获取行号信息    
        if(currentSourceManager->isInMainFile(startLoc)){
        unsigned startLine = currentSourceManager->getPresumedLineNumber(startLoc);    
        unsigned endLine = currentSourceManager->getPresumedLineNumber(endLoc);   
        std::cout << "Line" << startLine << ":" << endLine << std::endl; 	     
        }

        return true;// 继续遍历  
    }

    bool VisitIfStmt(SwitchStmt *stmt) {    
        SourceLocation startLoc = stmt->getBeginLoc();
        SourceLocation endLoc = stmt->getEndLoc();
        // 获取行号信息    
        if(currentSourceManager->isInMainFile(startLoc)){
        unsigned startLine = currentSourceManager->getPresumedLineNumber(startLoc);    
        unsigned endLine = currentSourceManager->getPresumedLineNumber(endLoc);   
        std::cout << "Line" << startLine << ":" << endLine << std::endl; 	     
        }

        return true;// 继续遍历  
    }

  std::string getCurrentFunctionName() {
    return currentFunctionName;
  }

  void setSourceManager(SourceManager* SM) {
    currentSourceManager = SM;
  }

private:
  std::string currentFunctionName = "";
  SourceManager* currentSourceManager = nullptr;
};

class MyASTConsumer : public ASTConsumer {
public:
  MyASTConsumer() : visitor() {}

  void HandleTranslationUnit(ASTContext& context) override {
    const FunctionDecl* FD = nullptr;
      for (const Decl* D : context.getTranslationUnitDecl()->decls()) {
          if (const auto* Function = dyn_cast<FunctionDecl>(D)) {
              FD = Function;
              break;
      }
}
    SourceLocation loc = FD->getLocation();
    visitor.setSourceManager(&context.getSourceManager());
    visitor.TraverseDecl(context.getTranslationUnitDecl());
  }

private:
  MyASTVisitor visitor;
};

class MyFrontendAction : public ASTFrontendAction {
public:
  std::unique_ptr<ASTConsumer> CreateASTConsumer(CompilerInstance& compilerInstance, StringRef file) override {
    return std::unique_ptr<ASTConsumer>(new MyASTConsumer);
  }
};

int main(int argc, const char* argv[]) {

  auto optionsParser = CommonOptionsParser::create(argc, argv, MyToolCategory);
  ClangTool tool(optionsParser->getCompilations(), optionsParser->getSourcePathList());
  int result = tool.run(newFrontendActionFactory<MyFrontendAction>().get());

  return result;
}
