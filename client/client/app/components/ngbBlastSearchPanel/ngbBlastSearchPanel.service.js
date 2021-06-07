const BLAST_STATES = {
    HISTORY: 'HISTORY',
    RESULT: 'RESULT',
    SEARCH: 'SEARCH'
};

export default class ngbBlastSearchService {
    static instance(dispatcher, bamDataService, projectDataService, ngbBlastSearchFormConstants) {
        return new ngbBlastSearchService(dispatcher, bamDataService, projectDataService, ngbBlastSearchFormConstants);
    }

    _detailedRead = null;
    _totalPagesCountHistory = 0;
    _currentResultId = null;
    _currentSearchId = null;
    _currentTool = null;

    get totalPagesCountHistory() {
        return this._totalPagesCountHistory;
    }

    set totalPagesCountHistory(value) {
        this._totalPagesCountHistory = value;
    }

    get blastStates() {
        return BLAST_STATES;
    }

    constructor(dispatcher, bamDataService, projectDataService, ngbBlastSearchFormConstants) {
        Object.assign(this, {dispatcher, bamDataService, projectDataService, ngbBlastSearchFormConstants});
        this.currentTool = this.ngbBlastSearchFormConstants.BLAST_TOOLS[0];
    }

    async getOrganismList(term, selectedOrganisms = []) {
        const selectedIds = selectedOrganisms.map(value => value.taxid);
        const organismList = await this.projectDataService.getOrganismList(term);
        return organismList.filter(value => !selectedIds.includes(value.taxid));
    }

    async getBlastDBList() {
        return await this.projectDataService.getBlastDBList('NUCLEOTIDE');
    }

    async _getDetailedRead() {
        const searchRequest = JSON.parse(localStorage.getItem('blastSearchRequest')) || null;
        let read = null;
        if (searchRequest) {
            switch (searchRequest.source) {
                case 'bam': {
                    read = await this.bamDataService.loadRead(searchRequest);
                    break;
                }
                case 'gene': {
                    read = {};
                    break;
                }
            }
        }
        return read;
    }


    set currentSearchId(currentSearchId) {
        this._currentSearchId = currentSearchId;
    }

    get currentResultId() {
        return this._currentResultId;
    }

    set currentResultId(currentResultId) {
        this._currentResultId = currentResultId;
    }

    get currentTool() {
        return this._currentTool;
    }

    set currentTool(tool) {
        this._currentTool = tool;
    }

    async getCurrentSearch() {
        let data = {};
        if (this._currentSearchId) {
            data = this._formatServerToClient(await this.projectDataService.getBlastSearch(this._currentSearchId));
        } else {
            const newSearch = await this._getDetailedRead();
            if (newSearch) {
                data.sequence = newSearch.sequence;
            }
            if (this.currentTool) {
                data.tool = this.currentTool;
            }
        }
        if (!data.organisms) {
            data.organisms = [];
        }
        return data;
    }

    async getCurrentSearchResult() {
        let data = {};
        if (this.currentResultId) {
            data = this._formatServerToClient(await this.projectDataService.getBlastSearch(this.currentResultId));
        }
        return data;
    }

    createSearchRequest(searchRequest) {
        searchRequest.organisms = searchRequest.organisms ? searchRequest.organisms.map(o => o.taxid) : [];
        return this.projectDataService.createBlastSearch(this._formatClientToServer(searchRequest)).then(data => {
            if (data && data.id) {
                this.currentSearchId = data.id;
                localStorage.removeItem('blastSearchRequest');
            }
            this.currentSearchId = null;
            this.currentTool = this.ngbBlastSearchFormConstants[0];
            return data;
        });
    }

    _formatServerToClient(search) {
        const result = {
            id: search.id,
            title: search.title,
            algorithm: search.algorithm,
            organisms: search.organisms,
            db: search.database,
            tool: search.executable,
            sequence: search.query,
            state: search.status,
            reason: search.statusReason,
            options: search.options,
            submitted: new Date(`${search.createdDate} UTC`)
        };
        if (search.excludedOrganisms) {
            result.organisms = search.excludedOrganisms.map(oId => ({taxid: oId}));
            result.isExcluded = true;
        } else {
            result.organisms = search.organisms ? search.organisms.map(oId => ({taxid: oId})) : [];
            result.isExcluded = false;
        }
        if (search.parameters) {
            result.maxTargetSeqs = search.parameters.max_target_seqs;
            result.threshold = search.parameters.evalue;
        }
        return result;
    }

    _formatClientToServer(search) {
        const result = {
            title: search.title || '',
            algorithm: search.algorithm,
            database: search.db,
            executable: search.tool,
            query: search.sequence,
            parameters: {}
        };
        if (search.isExcluded) {
            result.excludedOrganisms = search.organisms || [];
        } else {
            result.organisms = search.organisms || [];
        }
        if (search.maxTargetSeqs) {
            result.parameters.max_target_seqs = search.maxTargetSeqs;
        }
        if (search.threshold) {
            result.parameters.evalue = search.threshold;
        }
        if (search.options) {
            result.options = search.options;
        }
        return result;
    }
}
