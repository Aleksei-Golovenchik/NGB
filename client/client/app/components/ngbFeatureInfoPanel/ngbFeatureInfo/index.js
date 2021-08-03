import angular from 'angular';

// Import Style
import './ngbInfoRows/ngbInfoRows.scss';
import './ngbInfoTable/ngbInfoTable.scss';
import './ngbFeatureInfoMain/ngbFeatureInfoMain.scss';
import './ngbFeatureInfo.scss';


// Import internal modules
import ngbInfoRows from './ngbInfoRows/ngbInfoRows.component';
import ngbInfoTable from './ngbInfoTable/ngbInfoTable.component';
import constant from './ngbFeatureInfo.constant';
import ngbInfoService from './ngbFeatureInfo.service';
import ngbInfoController from './ngbFeatureInfo.controller';
import ngbFeatureInfoComponent from './ngbFeatureInfo.component';

import dataServices from '../../../../dataServices/angular-module';
import ngbFeatureInfoMainController from './ngbFeatureInfoMain/ngbFeatureInfoMain.controller';
import ngbFeatureInfoMainComponent from './ngbFeatureInfoMain/ngbFeatureInfoMain.component';
import ngbInfoRowsController from './ngbInfoRows/ngbInfoRows.controller';

export default angular.module('ngbFeatureInfo' , [ dataServices ])
    .constant('ngbFeatureInfoConstant', constant)
    .service('ngbFeatureInfoService', ngbInfoService.instance)
    .controller(ngbInfoController.UID, ngbInfoController)
    .controller(ngbFeatureInfoMainController.UID, ngbFeatureInfoMainController)
    .controller(ngbInfoRowsController.UID, ngbInfoRowsController)
    .component('ngbInfoRows', ngbInfoRows)
    .component('ngbInfoTable', ngbInfoTable)
    .component('ngbFeatureInfoMain', ngbFeatureInfoMainComponent)
    .component('ngbFeatureInfo', ngbFeatureInfoComponent)
    .name;
