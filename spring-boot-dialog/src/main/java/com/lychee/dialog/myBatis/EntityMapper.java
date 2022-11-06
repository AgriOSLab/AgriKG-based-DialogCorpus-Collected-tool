package com.lychee.dialog.myBatis;

import com.lychee.dialog.myBatis.Entitys.Dialog;
import com.lychee.dialog.myBatis.Entitys.Entity;
import com.lychee.dialog.myBatis.Entitys.Intention;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EntityMapper {

    @Select("select * from entities where id=#{id}")
    Entity getEntityById(@Param("id") int id);

    @Insert("insert into dialogs(record_id, turn_id, question, question_entity, answer, " +
            "answer_entities, intention, triples, labels, submitted_person) values(" +
            "#{record_id}, #{turn_id}, #{question}, #{question_entity}, #{answer}, " +
            "#{answer_entities}, #{intention}, #{triples}, #{labels}, #{submitted_person})")
    boolean insertRecord(Dialog dialog);

    @Update("update  entities set rejected=1 where id=#{id}")
    void setEntityRejected(@Param("id") int id);

    @Select("select max(record_id) from dialogs")
    Integer getMaxRecordId();


    @Select("select concat(id, ' ', intention) as label, intention as value from userintentions;")
    List<Intention> getIntentions();


    @Update("update entities set used=1 where id=#{id}")
    void setEntityUsed(@Param("id") int id);
}
