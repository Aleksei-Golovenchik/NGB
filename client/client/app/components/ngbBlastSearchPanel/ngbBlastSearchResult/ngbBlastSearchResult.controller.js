import baseController from '../../../shared/baseController';

export default class ngbBlastSearchResult extends baseController {

    static get UID() {
        return 'ngbBlastSearchResult';
    }

    readSequence = '';
    searchResult = {};
    isProgressShown = true;

    constructor(dispatcher, $scope, $timeout, ngbBlastSearchService) {
        super(dispatcher);

        Object.assign(this, {
            $scope,
            $timeout,
            dispatcher,
            ngbBlastSearchService
        });

        this.initEvents();
        this.initialize();
    }

    async initialize() {
        this.searchResult = await this.ngbBlastSearchService.getCurrentSearchResult();
        this.isProgressShown = false;
    }
}
