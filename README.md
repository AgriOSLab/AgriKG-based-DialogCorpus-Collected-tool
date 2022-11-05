# AgriKG-based-DialogCorpus-Collected-tool
JavaWeb tools for collecting corpus
1. 项目用于制作对话系统语料库，基于Spring boot和VUE实现前后端分离；
2. spring-boot-dialog是基于spring boot的后端项目，vue-dialog是基于VUE+element+vis的前端项目；
3. 使用Neo4j存储知识图谱，MySQL存储知识图谱的实体信息、关系信息，以及存储基于知识图谱人工构建的对话预料库；
4. 后端使用Redis作为缓存，实现当多用户同时在线标注时，系统能够有序的分配实体以及对应子图，并且能够有效的避免陷入淘汰实体回路；
