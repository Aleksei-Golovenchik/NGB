import angular from 'angular';

export default class ngbBlastSearchAlignmentList {

    static get UID() {
        return 'ngbBlastSearchAlignmentList';
    }

    searchResult = {};
    windowElm = {};
    isProgressShown = true;

    constructor(ngbBlastSearchService, $timeout, $window) {
        Object.assign(this, {
            ngbBlastSearchService, $timeout
        });
        this.windowElm = angular.element($window);
        this.initialize();
    }

    get ncbiUrl () {
        if (this.searchResult) {
            const id = this.searchResult.sequenceAccessionVersion || this.searchResult.sequenceId;
            const dbType = this.search && /^protein$/i.test(this.search.dbType)
                ? 'protein'
                : 'nucleotide';
            return `https://www.ncbi.nlm.nih.gov/${dbType}/${id}`;
        }
        return undefined;
    }

    initialize() {
        this.searchResult = this.ngbBlastSearchService.popCurrentAlignmentObject();
        this.search = this.ngbBlastSearchService.cutCurrentResult;
        // Todo: this is workaround for alignment rendering optimization.
        // We should check this and refactor
        this.$timeout(() => {
            this.windowElm.resize();
            this.$timeout(() => {
                this.windowElm.resize();
                this.$timeout(() => this.isProgressShown = false, 0);
            }, 0);
        }, 0);
    }
}
