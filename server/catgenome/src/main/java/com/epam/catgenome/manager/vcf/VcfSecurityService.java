/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2018 EPAM Systems
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.epam.catgenome.manager.vcf;

import com.epam.catgenome.controller.vo.registration.FeatureIndexedFileRegistrationRequest;
import com.epam.catgenome.entity.track.Track;
import com.epam.catgenome.entity.vcf.Variation;
import com.epam.catgenome.entity.vcf.VariationQuery;
import com.epam.catgenome.entity.vcf.VcfFile;
import com.epam.catgenome.entity.vcf.VcfFilterInfo;
import com.epam.catgenome.exception.FeatureFileReadingException;
import com.epam.catgenome.exception.FeatureIndexException;
import com.epam.catgenome.exception.VcfReadingException;
import com.epam.catgenome.security.acl.aspect.AclMaskList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.epam.catgenome.security.acl.SecurityExpressions.*;

@Service
public class VcfSecurityService {

    private static final String READ_VCF_BY_ID =
            "hasPermission(#vcfFileId, com.epam.catgenome.entity.vcf.VcfFile, 'WRITE')";
    private static final String READ_VCF_BY_TRACK_ID =
            "hasPermission(#variationTrack.id, com.epam.catgenome.entity.vcf.VcfFile, 'READ')";
    private static final String READ_VCF_BY_QUERY_ID =
            "hasPermission(#query.id, com.epam.catgenome.entity.vcf.VcfFile, 'READ')";

    @Autowired
    private VcfManager vcfManager;

    @Autowired
    private VcfFileManager vcfFileManager;


    @PreAuthorize(ROLE_ADMIN + OR + ROLE_VCF_MANAGER)
    public VcfFile registerVcfFile(FeatureIndexedFileRegistrationRequest request) {
        return vcfManager.registerVcfFile(request);
    }

    @PreAuthorize(ROLE_ADMIN + OR + READ_VCF_BY_ID)
    public VcfFile reindexVcfFile(long vcfFileId, boolean createTabixIndex) throws FeatureIndexException {
        return vcfManager.reindexVcfFile(vcfFileId, createTabixIndex);
    }

    @PreAuthorize(ROLE_ADMIN + OR + ROLE_VCF_MANAGER)
    public VcfFile unregisterVcfFile(long vcfFileId) throws IOException {
        return vcfManager.unregisterVcfFile(vcfFileId);
    }

    @AclMaskList
    @PostFilter(ROLE_ADMIN + OR + READ_ON_FILTER_OBJECT)
    public List<VcfFile> loadVcfFilesByReferenceId(Long referenceId) {
        return vcfFileManager.loadVcfFilesByReferenceId(referenceId);
    }

    @PreAuthorize(ROLE_ADMIN + OR + READ_VCF_BY_TRACK_ID)
    public Track<Variation> loadVariations(Track<Variation> variationTrack, Long sampleId, boolean loadInfo,
                                 boolean collapsed) throws VcfReadingException {
        return vcfManager.loadVariations(variationTrack, sampleId, loadInfo, collapsed);
    }

    @PreAuthorize(ROLE_USER)
    public Track<Variation> loadVariations(final Track<Variation> track, String fileUrl, String indexUrl,
                                           final Integer sampleIndex, final boolean loadInfo,
                                           final boolean collapse) throws VcfReadingException {
        return vcfManager.loadVariations(track, fileUrl, indexUrl, sampleIndex, loadInfo, collapse);
    }

    @PreAuthorize(ROLE_ADMIN + OR + READ_VCF_BY_QUERY_ID)
    public Variation loadVariation(VariationQuery query) throws FeatureFileReadingException {
        return vcfManager.loadVariation(query);
    }

    @PreAuthorize(ROLE_USER)
    public Variation loadVariation(VariationQuery query, String fileUrl, String indexUrl)
            throws FeatureFileReadingException {
        return vcfManager.loadVariation(query, fileUrl, indexUrl);
    }

    @PreAuthorize(ROLE_USER)
    public Variation getNextOrPreviousVariation(int fromPosition, Long trackId, Long sampleId, long chromosomeId,
                                                boolean loadInfo, String fileUrl, String indexUrl)
                                                throws VcfReadingException {
        return vcfManager.getNextOrPreviousVariation(fromPosition, trackId, sampleId, chromosomeId,
                                                     loadInfo, fileUrl, indexUrl);
    }

    @PreAuthorize(ROLE_USER)
    @PreFilter(ROLE_ADMIN + OR + VCF_FILE_FILTER_BY_ID)
    public VcfFilterInfo getFiltersInfo(List<Long> fileIds) throws IOException {
        return vcfManager.getFiltersInfo(fileIds);
    }
}
