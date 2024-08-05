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

# Taint Analysis tool

Conduct taint analysis using the LLMAST tool and Javaparser to locate target variables, functions, or specific code structures. The detailed instructions are in the folder taintanalysis.
