export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v8 = "testTag";
        const v9 = "Failed to load the content. Cause: %{public}s";
        const v11 = JSON;
        v11.stringify(v16);
        stringify = v11.stringify(v12);
        v6.error(v7, v8, v9);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        try {
        } catch (e) {
            const error = v8.error;
            const v10 = "testTag";
            const v11 = "Failed to set colorMode. Cause: %{public}s";
            const v13 = JSON;
            let stringify = v13.stringify;
            const v14 = v6;
            v13.stringify(v14);
            stringify = v13.stringify(v14);
            v8.error(v9, v10, v11);
        }
        const v9 = "testTag";
        const v10 = "%{public}s";
        const v11 = "Ability onCreate";
        error.info(v8, v9, v10);
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v9 = "%{public}s";
        v6.info(v7, v8, v9);
        const v6 = v3;
        const v7 = "pages/Index";
        v8 = onBackground;
        v6.loadContent(v7, v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        try {
            throw v5;
        } catch (e) {
            throw v3;
        }
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        try {
            throw v6;
        } catch (e) {
            throw v4;
        }
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v8;
        v2.value = 0;
        v5 = v2;
        v5.steps = 0;
        v5 = v2;
        v5.value = v9;
        v5 = v2;
        v5.steps = 0;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 * 2;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 + 3;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 - 1;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static then(param_0, param_1, param_2, param_3): void     {
        v2 = v10;
        v2.value = v11(v2.value);
        v6 = v2;
        const steps = v6.steps;
        const number: number = Number(steps);
        v6.steps = _number + 1;
        return v2;
    }

    static swapPair(param_0, param_1, param_2, param_3): void     {
        let v8 = v3;
        let v9 = 0;
        const v4 = v8[v9];
        v8 = v13;
        v9 = 1;
        const v6 = v8[v9];
        v8 = [];
        v6[8] = v10;
        v4[8] = v11;
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        v7.create(lex_0_0.message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v21;
        v1 = v22;
        lex_0_3 = func_main_0;
        v13 = anonymous_method;
        lex_0_5 = v13;
        v13 = func_main_0;
        lex_0_1 = v13;
        v13 = onCreate;
        lex_0_2 = v13;
        v13 = onDestroy;
        lex_0_6 = v13;
        v13 = EntryAbility;
        lex_0_4 = v13;
        const v15 = ViewPU;
        const v15 = Reflect;
        let set = v15.set;
        v15.set(ViewPU.prototype, "finalizeConstruction", func_main_0);
        prototype = undefined;
        let v0: @system.app = @system.app;
        let isUndefined: boolean = set != undefined;
        prototype = set.prototype;
        v18 = rerender;
        set.v18();
        set = set;
        set = set;
        prototype = undefined;
        let v1: @system.app = @system.app;
        isUndefined = set != undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        isUndefined = set != undefined;
        prototype = set.prototype;
        v18 = "message";
        v19 = undefined;
        false[v20] = "get".v19;
        v18 = "message";
        v19 = undefined;
        v20 = testMergeConfig;
        false[v19] = "get".v20;
        prototype2.initialRender = getEntryName;
        prototype2.rerender = initialRender;
        set.getEntryName = testOptionalChaining;
        set = set;
        set = set;
        set = registerNamedRoute;
        isUndefined = func_52;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static getCitySafe(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === null)) {
            const isNull: boolean = v5 === null;
            let address = isNull.address;
            if (!(address === null)) {
                const isNull: boolean = address === null;
                address = isNull.address;
                return address.city;
            }
            return "N/A";
        }
        return "N/A";
    }

    static mergeConfig(param_0, param_1, param_2, param_3, param_4): void     {
        {  }.host = v12.host;
        v8 = v12;
        v7.port = v8.port;
        v8 = v12;
        v7.debug = v8.debug;
        const v5 = v7;
        v8 = v13;
        const host = v8.host;
        v8 = v12;
        if (host !== v8.host) {
            const host = v5;
            let v8 = v4;
            host.host = v8.host;
            let v8 = v4;
            const port = v8.port;
            v8 = v3;
            if (port !== v8.port) {
                const port = v5;
                let v8 = v4;
                port.port = v8.port;
            }
            return port !== v8.port;
        }
        let hasHost: boolean = host !== v8.host;
        const debug = hasHost.debug;
        hasHost = v12;
        const debug = v5;
        let hasHost = v4;
        debug.debug = hasHost.debug;
    }

    static getSteps(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.steps;
    }

    static getValue(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.value;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static getEmailSafe(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === null)) {
            const isNull: boolean = v5 === null;
            const email = isNull.email;
            if (!(email === null)) {
                const isNull: boolean = email === null;
                return isNull2.email;
            }
            return "no email";
        }
        return "no email";
    }

    static testCoalesce(param_0, param_1, param_2): void     {
        let v7 = lex_0_3;
        v7 = v7(1, 2, 3);
        v7 = lex_0_3;
        v8 = null;
        v9 = 5;
        v10 = 10;
        v7 = v7(v8, v9, v10);
        v7 = lex_0_3;
        v8 = null;
        v9 = null;
        v10 = 7;
        v7 = v7(v8, v9, v10);
        let v11 = String;
        v11 = v11(v7);
        v9 = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
    }

    static isSuccess(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const v6 = v3;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static coalesceChain(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v13;
        const v7 = v3;
        if (!(v7 !== null)) {
            const isNull: boolean = v7 !== null;
            if (isNull !== null) {
                return isNull !== null;
            }
            return v4;
        }
        return v11;
    }

    static extractMiddle(param_0, param_1, param_2, param_3): void     {
        const v7 = v3;
        const length = v7.length;
        if (!(length < 3)) {
            const length: boolean = length < 3;
            const v7 = 1;
            const v4 = length[v7];
            return v4;
        }
        const length = 1;
        return -length;
    }

    static getMessage(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 200)) {
            const v5: boolean = v5 === 200;
            if (!(v5 === 404)) {
                const v5: boolean = v5 === 404;
                return "Server Error";
            }
            return "Not Found";
        }
        return "OK";
    }

    static testHttpStatus(param_0, param_1, param_2): void     {
        v9.getMessage(200);
        message = v9.getMessage(v10);
        message = v9.getMessage;
        v10 = 404;
        v9.getMessage(v10);
        message = v9.getMessage(v10);
        let success = v9.isSuccess;
        v10 = 200;
        v9.isSuccess(v10);
        success = v9.isSuccess(v10);
        success = v9.isSuccess;
        v10 = 500;
        v9.isSuccess(v10);
        success = v9.isSuccess(v10);
        v10 = `${`${message2},` + message3},`;
        v11 = String;
        v12 = success2;
        let v9: number = v10 + v11(v12);
        success = `${v9},`;
        v9 = String;
        v10 = success3;
        return success + v9(v10);
    }

    static testDestructure(param_0, param_1, param_2): void     {
        let v6 = lex_0_4;
        let v7: Array<unknown> = [];
        v7 = [];
        v6 = v6(v7);
        v6 = lex_0_5;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v6 = v6(v7);
        const v9 = v6;
        let join = v9.join;
        v9.join(",");
        join = v9.join(v10);
        v6 = `${join2}|`;
        const join2 = String;
        join = v62;
        return v6 + join2(join);
    }

    static testMergeConfig(param_0, param_1, param_2): void     {
        let v7 = {  };
        const v3 = v7;
        v7 = {  };
        const v5 = v7;
        v7 = lex_0_6;
        v7 = v7(v3, v5);
        v9 = `${v7.host}:`;
        host = String;
        v8 = v9 + host(v7.port);
        v7 = `${v8},`;
        v8 = String;
        const debug = v9.debug;
        return v7 + v8(debug);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testPromiseChain(param_0, param_1, param_2): void     {
        let v6 = 5;
        v6 = new 5();
        let then = v6.then;
        v6.then(onDestroy);
        then = v3.then;
        v7 = onWindowStageDestroy;
        v3.then(v7);
        then = v3.then;
        v7 = anonymous_method;
        v3.then(v7);
        v7 = String;
        const v9 = v3;
        let value = v9.getValue;
        v9.getValue();
        value = v9.getValue();
        v7 = v7(value);
        then = `${v7},`;
        v7 = String;
        value = v3;
        let steps = value.getSteps;
        value.getSteps();
        steps = value.getSteps();
        return then + v7(steps);
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5.observeComponentCreation2(func_main_0, Column);
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5.observeComponentCreation2(v6, v7);
        v5 = Text;
        v5.pop();
        v5 = Column;
        pop = v5.pop;
        v5.pop();
        return;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.aboutToBeDeleted();
        let v7 = SubscriberManager;
        v7.Get();
        get = v7.Get();
        let id__ = v7.id__;
        v10.id__();
        id__ = v10.id__();
        get2.delete(id__);
        const get2 = v2;
        get2.aboutToBeDeletedInternal();
        return;
    }

    static testOptionalChaining(param_0, param_1, param_2): void     {
        {  }.address = {  };
        const v7 = v10;
        v10 = {  };
        const v8 = v10;
        v10 = lex_0_1;
        v11 = v7;
        v10 = v10(v11);
        v10 = lex_0_1;
        v11 = v8;
        v10 = v10(v11);
        v10 = lex_0_2;
        v11 = v7;
        v10 = v10(v11);
        v10 = lex_0_2;
        v11 = v8;
        v10 = v10(v11);
        v11 = `${`${v10},` + v102},` + v103;
        v10 = `${v11},`;
        return v10 + v104;
    }

    static static_initializer(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.OK = 200;
        v4.NOT_FOUND = 404;
        v4.SERVER_ERROR = 500;
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const message = v6.message;
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.purgeDependencyOnElmtId(v11);
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}