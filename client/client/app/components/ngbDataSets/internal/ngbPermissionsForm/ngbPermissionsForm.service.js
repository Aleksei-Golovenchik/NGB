export default class ngbPermissionsFormService {

    static instance(userDataService, roleDataService, permissionsDataService) {
        return new ngbPermissionsFormService(userDataService, roleDataService, permissionsDataService);
    }

    _userDataService;
    _roleDataService;
    _permissionsDataService;

    constructor(userDataService, roleDataService, permissionsDataService) {
        this._userDataService = userDataService;
        this._roleDataService = roleDataService;
        this._permissionsDataService = permissionsDataService;
    }

    static getNodeAclClass(node) {
        if (node.isProject) {
            return 'PROJECT';
        } else {
            return (node.format || '').toUpperCase();
        }
    }

    getUsers() {
        const getUserAttributesString = (user) => {
            const values = [];
            const firstAttributes = ['FirstName', 'LastName'];
            for (const key in user.attributes) {
                if (user.attributes.hasOwnProperty(key) && firstAttributes.indexOf(key) >= 0) {
                    values.push(user.attributes[key]);
                }
            }
            for (const key in user.attributes) {
                if (user.attributes.hasOwnProperty(key) && firstAttributes.indexOf(key) === -1) {
                    values.push(user.attributes[key]);
                }
            }
            return values.join(' ');
        };
        return this._userDataService.getUsers().then(users => {
            return (users || []).map(u => ({
                ...u,
                userAttributes: u.attributes ? getUserAttributesString(u) : undefined
            }));
        });
    }

    getRoles() {
        return this._roleDataService.getRoles();
    }

    getNodePermissions(node) {
        const aclClass = ngbPermissionsFormService.getNodeAclClass(node);
        const mapPermission = p => ({mask: p.mask, ...p.sid});
        return this._permissionsDataService
            .getObjectPermissions(node.id, aclClass)
            .then(data => {
                if (data) {
                    return {
                        mask: data.entity.mask,
                        owner: data.entity.owner,
                        permissions: (data.permissions || []).map(mapPermission)
                    };
                } else {
                    return {
                        error: true,
                        message: 'Error fetching object permissions'
                    };
                }
            });
    }

    deleteNodePermissions(node, userOrGroup) {
        const aclClass = ngbPermissionsFormService.getNodeAclClass(node);
        return this._permissionsDataService
            .deleteObjectPermissions(node.id, aclClass, userOrGroup.name, userOrGroup.principal);
    }

    grantPermission(node, userOrGroup, mask) {
        const aclClass = ngbPermissionsFormService.getNodeAclClass(node);
        return this._permissionsDataService
            .grantPermission(node.id, aclClass, userOrGroup.name, userOrGroup.principal, mask);
    }

    grantOwner(node, user) {
        const aclClass = ngbPermissionsFormService.getNodeAclClass(node);
        return this._permissionsDataService
            .grantOwner(node.id, aclClass, user);
    }

    searchAdGroups(prefix) {
        return this._roleDataService.findADGroup(prefix);
    }

    getPermissionsColumns() {
        return [{
            cellTemplate: `
                    <div layout="row" style="flex-flow: row wrap; justify-content: center; align-items: center; width: 100%">
                        <ng-md-icon ng-if="row.entity.principal" icon="person"></ng-md-icon>
                        <ng-md-icon ng-if="!row.entity.principal" icon="group"></ng-md-icon>
                    </div>
                `,
            enableColumnMenu: false,
            enableMove: false,
            enableSorting: false,
            field: 'principal',
            maxWidth: 50,
            minWidth: 50,
            name: ' ',
        }, {
            cellTemplate: `
                    <div class="ui-grid-cell-contents">
                        <ngb-user ng-if="row.entity.principal" user="row.entity.name" />
                        <span ng-if="!row.entity.principal">{{row.entity.displayName}}</span>
                    </div>
                `,
            enableColumnMenu: false,
            enableSorting: true,
            field: 'displayName',
            minWidth: 50,
            name: 'Name',
            width: '*',
        }, {
            cellTemplate: `
                      <div layout="row" style="flex-flow: row wrap; justify-content: center; align-items: center; width: 100%">
                          <md-button
                              ng-disabled="!grid.appScope.ctrl.permissionsChangeAllowed"
                              aria-label="Delete"
                              class="md-mini md-hue-1 grid-action-button"
                              ng-click="grid.appScope.ctrl.deleteSubjectPermissions(row.entity, $event)">
                              <ng-md-icon icon="delete"></ng-md-icon>
                          </md-button>
                      </div>`,
            enableColumnMenu: false,
            enableSorting: false,
            enableMove: false,
            field: 'actions',
            maxWidth: 120,
            minWidth: 120,
            name: ''
        }];
    }

}
