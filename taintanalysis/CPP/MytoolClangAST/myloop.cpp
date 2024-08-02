#include <iostream>
#include <string>
#include "clang/AST/AST.h"
#include "clang/AST/RecursiveASTVisitor.h"
#include "clang/Tooling/CommonOptionsParser.h"
#include "clang/Tooling/Tooling.h"
#include "clang/Frontend/CompilerInstance.h"

using namespace clang;
using namespace clang::tooling;
using namespace llvm;

static cl::OptionCategory MyToolCategory("my-tool options");
static cl::opt<std::string> FuncionName("name", cl::Required, cl::desc("Funciton name"),cl::cat(MyToolCategory));
// 自定义的 ASTVisitor
class LoopVisitor : public RecursiveASTVisitor<LoopVisitor> {
public:
  explicit LoopVisitor(ASTContext *Context) : Context(Context) {}
    // 遍历 ForStmt 节点
    bool VisitForStmt(ForStmt *stmt) {
    // 检测到循环结构，打印相关信息
    llvm::outs() << "Detected a for loop at line " << stmt->getBeginLoc().printToString(Context->getSourceManager()) << "end:"<< stmt->getEndLoc().printToString(Context->getSourceManager())<<"\n";
    return true;

  }

  // 遍历 WhileStmt 节点
  bool VisitWhileStmt(WhileStmt *stmt) {
    // 检测到循环结构，打印相关信息
    llvm::outs() << "Detected a while loop at line " << stmt->getBeginLoc().printToString(Context->getSourceManager()) << "end:"<< stmt->getEndLoc().printToString(Context->getSourceManager())<<"\n";
    return true;
  }

  // 遍历 DoStmt 节点
  bool VisitDoStmt(DoStmt *stmt) {
    // 检测到循环结构，打印相关信息
    llvm::outs() << "Detected a do-while loop at line " << stmt->getBeginLoc().printToString(Context->getSourceManager()) << "\n";
    return true;
  }



private:
  ASTContext *Context;
};

// 自定义的 ASTConsumer
class LoopASTConsumer : public ASTConsumer {
public:
  explicit LoopASTConsumer(ASTContext *Context, const std::string &FunctionName) : Visitor(Context), FunctionName(FunctionName) {}

  // 遍历指定函数的 AST
  bool HandleTopLevelDecl(DeclGroupRef DG) override {
    for (Decl *D : DG) {
      if (FunctionDecl *FD = dyn_cast<FunctionDecl>(D)) {
        // 只处理指定函数名的函数
        if (FD->getNameAsString() == FunctionName) {
          Visitor.TraverseDecl(D);
        }
      }
    }
    return true;
  }
  
private:
  LoopVisitor Visitor;
  std::string FunctionName;
};

class MyFrontendAction : public ASTFrontendAction {
public:
  std::unique_ptr<ASTConsumer> CreateASTConsumer(CompilerInstance &compilerInstance, StringRef file) override {
    ASTContext &context = compilerInstance.getASTContext();
    return std::unique_ptr<ASTConsumer>(new LoopASTConsumer(&context,FuncionName));
  }
};

// Clang 工具入口
int main(int argc, const char **argv) {
  // 解析命令行参数
  auto optionsParser = CommonOptionsParser::create(argc, argv,MyToolCategory);
  ClangTool Tool(optionsParser->getCompilations(), optionsParser->getSourcePathList());

Tool.run(newFrontendActionFactory<MyFrontendAction>().get());

  return 0;
}
