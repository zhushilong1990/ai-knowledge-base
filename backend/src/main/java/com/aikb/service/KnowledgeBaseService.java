package com.aikb.service;

import com.aikb.dto.CreateKBRequest;
import com.aikb.dto.KnowledgeBaseVO;
import com.aikb.entity.Document;
import com.aikb.entity.KnowledgeBase;
import com.aikb.mapper.DocumentMapper;
import com.aikb.mapper.KnowledgeBaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseMapper kbMapper;
    private final DocumentMapper docMapper;

    public KnowledgeBaseService(KnowledgeBaseMapper kbMapper, DocumentMapper docMapper) {
        this.kbMapper = kbMapper;
        this.docMapper = docMapper;
    }

    /**
     * List all knowledge bases for a user with document counts.
     */
    public List<KnowledgeBaseVO> listByUserId(Long userId) {
        List<KnowledgeBase> kbs = kbMapper.selectList(
            new QueryWrapper<KnowledgeBase>().eq("user_id", userId).orderByDesc("updated_at")
        );
        return kbs.stream().map(kb -> {
            KnowledgeBaseVO vo = new KnowledgeBaseVO();
            vo.setId(kb.getId());
            vo.setName(kb.getName());
            vo.setDescription(kb.getDescription());
            vo.setDocCount(docMapper.countByKbId(kb.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * Create a new knowledge base.
     */
    public KnowledgeBase create(CreateKBRequest request, Long userId) {
        KnowledgeBase kb = KnowledgeBase.builder()
            .userId(userId)
            .name(request.getName())
            .description(request.getDescription())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        kbMapper.insert(kb);
        return kb;
    }

    /**
     * List all documents in a knowledge base.
     */
    public List<Document> getDocuments(Long kbId, Long userId) {
        return docMapper.selectList(
            new QueryWrapper<Document>()
                .eq("knowledge_base_id", kbId)
                .eq("user_id", userId)
                .orderByDesc("created_at")
        );
    }
}
