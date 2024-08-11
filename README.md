# Abstract

> Configuration in software systems helps to ensure efficient operation and meet diverse user needs. Yet, some, if not all, configuration options have profound implications for the system’s performance. Configuration performance analysis, wherein the key is to understand (or infer) the configuration options’ relations and their impacts on performance, is crucial. Two major modalities exist that serve as the source information in the analysis: either the manual or source code. However, it re mains unclear what roles they play in configuration performance analysis. Much work that relies on manuals claims their benefits of information richness and naturalness; while work that trusts the source code more prefers the structural information provided therein and criticizes the timeliness of manuals. To fill such a gap, in this paper, we conduct an extensive empirical study over 10 systems, covering 1,694 options, 106,798 words in the manual, and 22,859,552 lines-of-code for investigating the usefulness of manual and code in two important tasks of configuration performance analysis, namely performance sensitive options identification and the associated dependencies extraction. We reveal several new findings and insights, such as it is beneficial to fuse the manual and code modalities for both tasks; the current automated tools that rely on a single modality are far from being practically useful and generally remain incomparable to human analysis. All those pave the way for further advancing configuration performance analysis.

# Documents

Specifically, the documents include:

## RQ1:

- **Systemoverview.csv:** An overview of all systems.
- **FalsePositive.csv:** Statistics of false positives from both manual analysis and code analysis.
- **FalseNegative.csv:** Statistics of false negatives from both manual analysis and code analysis.
- **FalsePositive:** Number and classification statistics of false positives for each software.

## RQ2:

- **DependencyOverview.csv:** Statistics of dependency relationships for all systems.
- **ManualDependency.csv:** Statistics of dependency relationships from manual analysis.
- **CodeDependency.csv:** Statistics of dependency relationships from code analysis.

## RQ3:
> Result of Safetune and Gptuner in each run.

## dataset
### groundtruth

> Performance test result for each system.

### dependency

> dependencies we found in each system.

# Taint Analysis tool
## CPPtools
### 1: Obtaining Clang

1. **Create a working directory and download the LLVM project:**

   ```
   mkdir ~/clang-llvm && cd ~/clang-llvm
   git clone https://github.com/llvm/llvm-project.git
   ```

   - Create a new directory and download LLVM source code.

2. **Download and install the Ninja build tool:**

   ```
   cd ~/clang-llvm
   git clone https://github.com/martine/ninja.git
   cd ninja
   git checkout release
   ./configure.py --bootstrap
   sudo cp ninja /usr/bin/
   ```

   - Download Ninja source code, build, and install it.

3. **Download and install CMake:**

   ```
   cd ~/clang-llvm
   git clone https://gitlab.kitware.com/cmake/cmake.git
   cd cmake
   git checkout next
   ./bootstrap
   make
   sudo make install
   ```

   - Download CMake source code, build, and install it.

4. **Build Clang:**

   ```
   cd ~/clang-llvm
   mkdir build && cd build
   cmake -G Ninja ../llvm-project/llvm -DLLVM_ENABLE_PROJECTS="clang;clang-tools-extra" -DCMAKE_BUILD_TYPE=Release -DLLVM_BUILD_TESTS=ON
   ninja
   ninja check
   ninja clang-test
   ninja install
   ```

   - Configure, build, test, and install Clang and related tools.

5. **Set Clang as the compiler:**

   ```
   cd ~/clang-llvm/build
   ccmake ../llvm-project/llvm
   ```

   - Open CMake configuration interface and set `CMAKE_CXX_COMPILER` to `/usr/bin/clang++`.

### 2: Create a ClangTool

1. **Create a new tool directory and add it to the CMake build system:**

   ```
   cd ~/clang-llvm/llvm-project
   mkdir clang-tools-extra/loop-convert
   echo 'add_subdirectory(loop-convert)' >> clang-tools-extra/CMakeLists.txt
   vim clang-tools-extra/loop-convert/CMakeLists.txt
   ```

   - Create a new directory and modify CMake files to include the new tool.

2. **Edit `CMakeLists.txt`:**

   ```
   set(LLVM_LINK_COMPONENTS support)
   
   add_clang_executable(loop-convert
     LoopConvert.cpp
     )
   target_link_libraries(loop-convert
     PRIVATE
     clangAST
     clangASTMatchers
     clangBasic
     clangFrontend
     clangSerialization
     clangTooling
     )
   ```

With that done, Ninja will be able to compile our tool. Let’s give it something to compile! Put the following into `clang-tools-extra/loop-convert/functionsearch.cpp`. 

1. functionsearch.cpp
2. varsearch.cpp
3. loopsearch.cpp
4. ifswitchlsearch.cpp

### 3: Compiling the New Tool

1. Compile the new tool:

   ```
   cd ~/clang-llvm/build
   ninja functionsearch
   ```

   - Run the `ninja` build tool from the build directory to compile the new tool.

### 4: Running the New Tool

After compiling, the new tool (syntax checker) will be located in the `~/clang-llvm/build/bin` directory.

### 5: New Tools Indroduction

functionsearch.cpp: Search for the location where the target function is called within the specified file and function.

```
./functionsearch -name {function} -fun {targetfunction} {file}
```

varsearch.cpp: Search for the target variable initialization location and the location where it is called within the specified file.

```
./varsearch -var {var} ./searchfold/redis-6.2.12/src/{file}
```

loopsearch.cpp:  Search for the locations where loop(while for) control flow appears in the specified file.

```
./loopsearch {file}
```

ifswitchlsearch.cpp: Search for the locations where if(switch) control flow appears in the specified file.

```
./ifswitchlsearch {file}
```
## JAVAtools
### Prerequisites

> openjdk 1.8.0_422
>
> javaparser 3.15.15

### JAVATools Indroduction

> **varsearch.java**: Search for the target variable initialization location and the location where it is called within the specified file.
>
> **controlflowsearch.java**: Search for the locations where control flow(while for switch if try) appears in the program.
>
> **functionsearch.java**: Search for the location where the target function is called within the program.

