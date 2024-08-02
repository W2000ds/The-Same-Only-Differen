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
static cl::opt<std::string> TargetVariable("var", cl::Required, cl::desc("Target variable to find usage"), cl::cat(MyToolCategory));

class MyASTVisitor : public RecursiveASTVisitor<MyASTVisitor> {
public:
  bool VisitDeclRefExpr(DeclRefExpr* E) {
    if (std::find(targetVariables.begin(), targetVariables.end(), E->getNameInfo().getAsString()) != targetVariables.end()) {
      llvm::errs() << "Variable used in function: " << getCurrentFunctionName() << "\n";
    }
    return true;
  }
  
  bool VisitMemberExpr(MemberExpr* E) {
  // 获取结构体变量的名字
  Expr* base = E->getBase();
  if (DeclRefExpr* DRE = dyn_cast<DeclRefExpr>(base)) {
    std::string structName = DRE->getNameInfo().getAsString();
    // 获取结构体成员的名字
    std::string memberName = E->getMemberNameInfo().getAsString();
    // 拼接成完整的名字
    std::string fullName = structName + "." + memberName;
    // 检查是否是目标变量之一
    if (std::find(targetVariables.begin(), targetVariables.end(), fullName) != targetVariables.end()) {
      llvm::errs() << "Variable used in function: " << getCurrentFunctionName() << "\n";
    }
  }
  return true;
}

  bool VisitFunctionDecl(FunctionDecl* FD) {
    currentFunctionName = FD->getNameAsString();
    return true;
  }

  std::string getCurrentFunctionName() {
    return currentFunctionName;
  }

  std::string getCurrentFilename() {
    std::string filename = "";
    if (currentSourceManager != nullptr) {
      filename = currentSourceManager->getFilename(currentSourceLocation).str();
    }
    return filename;
  }

  void setSourceManager(SourceManager* SM) {
    currentSourceManager = SM;
  }

  void setCurrentLocation(SourceLocation loc) {
    currentSourceLocation = loc;
  }

  void setTargetVariables(const std::vector<std::string>& targetVars) {
    targetVariables = targetVars;
  }

private:
  std::string currentFunctionName = "";
  SourceManager* currentSourceManager = nullptr;
  SourceLocation currentSourceLocation;
  std::vector<std::string> targetVariables;
};

class MyASTConsumer : public ASTConsumer {
public:
  MyASTConsumer() : visitor() {}

  void HandleTranslationUnit(ASTContext& context) override {
    visitor.setTargetVariables({ TargetVariable });
    //visitor.setSourceManager(&context.getSourceManager());
    //const FunctionDecl* FD = context.getTranslationUnitDecl()->getSingleDecl<FunctionDecl>();
    const FunctionDecl* FD = nullptr;
      for (const Decl* D : context.getTranslationUnitDecl()->decls()) {
          if (const auto* Function = dyn_cast<FunctionDecl>(D)) {
              FD = Function;
              break;
      }
}
    SourceLocation loc = FD->getLocation();
    visitor.setCurrentLocation(loc);
    visitor.setSourceManager(&context.getSourceManager());
    //TraverseDecl(context.getTranslationUnitDecl());
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
