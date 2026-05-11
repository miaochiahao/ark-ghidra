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

    constructor(param_0, param_1, param_2)     {
        return v6;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        v6.startsWith("c");
        return v6.startsWith(v7);
    }

    static merge(param_0, param_1, param_2, param_3, param_4): void     {
        let v9: Array<unknown> = [];
        const v7: Array<unknown> = [];
        const v5 = 0;
        const v6 = 0;
        v9 = v5;
        let v10 = v3;
        let v9 = v6;
        let v10 = v4;
        v10 = v16;
        v9 = v10[v11];
        v10 = v17;
        v11 = v6;
        if (!(v10[v11] <= v9)) {
            let v10: boolean = v10[v11] <= v9;
            let push = v10.push;
            let v11 = v4;
            const v12 = v6;
            v11 = v11[v12];
            v10.push(v11);
            push = v6;
            const v6: number = 1 + push;
        }
        let v10 = v7;
        push = v10.push;
        let v11 = v3;
        v11 = v11[v12];
        v10.push(v11);
        push = v5;
        const v5: number = 1 + push;
        let push: number = 1 + push;
        let v10 = v3;
        while (!(v10.length < push)) {
            loop_0: do {
    let hasPush: boolean = v10.length < push;
    let v10 = v3;
} while (v10.length < hasPush);
        }
        const hasHasPush: boolean = v10.length < hasPush;
        let v10 = v4;
        while (!(v10.length < hasHasPush)) {
            loop_1: do {
    let hasHasHasPush: boolean = v10.length < hasHasPush;
    let v10 = v4;
} while (v10.length < hasHasHasPush);
        }
        return v10.length < hasHasHasPush;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        v6.startsWith("z");
        return v6.startsWith(v7);
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

    static testTrim(param_0, param_1, param_2): void     {
        const v4 = "  hello  ";
        let v9: string = v4;
        let trim = v9.trim;
        v9.trim();
        trim = v9.trim();
        v9 = "xxworldxx";
        trim = v9.trim;
        v9.trim();
        trim = v9.trim();
        let v10 = String;
        v10 = v10(trim2.length);
        trim = `,${v10}`;
        return trim3 + trim;
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
        v0 = v20;
        lex_0_3 = func_main_0;
        v12 = anonymous_method;
        lex_0_2 = v12;
        v12 = func_main_0;
        lex_0_1 = v12;
        const v14 = ViewPU;
        const v14 = Reflect;
        let set = v14.set;
        v14.set(ViewPU.prototype, "finalizeConstruction", anonymous_method);
        prototype = undefined;
        let v0: @system.app = @system.app;
        let isUndefined: boolean = set == undefined;
        prototype = set.prototype;
        v17 = anonymous_method;
        set.v17();
        set = set;
        set = set;
        prototype = ViewPU;
        isUndefined = set == undefined;
        prototype = set.prototype;
        v17 = "message";
        v18 = undefined;
        false[v19] = "get".v18;
        v17 = "message";
        v18 = undefined;
        v19 = initialRender;
        false[v18] = "get".v19;
        prototype2.initialRender = testSetOperations;
        prototype2.rerender = testOptionalChaining;
        set.getEntryName = purgeVariableDependenciesOnElmtId;
        set = set;
        set = set;
        set = registerNamedRoute;
        isUndefined = func_47;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static mergeSort(param_0, param_1, param_2, param_3): void     {
        const v11 = v3;
        return v17;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static toString(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 200)) {
            const v5: boolean = v5 === 200;
            if (!(v5 === 404)) {
                const v5: boolean = v5 === 404;
                return "ERROR";
            }
            return "NOT_FOUND";
        }
        return "OK";
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testFindIndex(param_0, param_1, param_2): void     {
        const v4: Array<unknown> = [];
        let v8: Array<unknown> = v4;
        let findIndex = v8.findIndex;
        v8.findIndex(onCreate);
        findIndex = v8.findIndex(v9);
        findIndex = v4.findIndex;
        v9 = onWindowStageCreate;
        v4.findIndex(v9);
        findIndex = v4.findIndex(v9);
        v9 = String;
        v9 = v9(findIndex2);
        findIndex = `,${v9}`;
        v9 = String;
        v9 = findIndex3;
        return v9(v9) + findIndex;
    }

    static testMergeSort(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = lex_0_1;
        v6 = v6(v3);
        let v10 = String;
        let v11: Array<unknown> = v6;
        v11 = v11[v12];
        v10 = v10(v11);
        let v8: string = `,${v10}`;
        v10 = String;
        v10 = v6;
        v11 = 3;
        v10 = v10[v11];
        v7 = v10(v10) + v8;
        v6 = `,${v7}`;
        v7 = String;
        v8 = v6;
        v10 = 6;
        v8 = v8[v10];
        return v7(v8) + v6;
    }

    static getDisplayName(param_0, param_1, param_2, param_3): void     {
        const v8 = v3;
        const name = v8.name;
        const name = v8.name;
        const name2 = name;
        if (name2 === undefined) {
        } else {
            const isUndefined: boolean = name2 === undefined;
        }
        const name2 = v3;
        const email = name2.email;
        const email = name2.email;
        const email2 = email;
        if (email2 === undefined) {
        } else {
            const isUndefined: boolean = email2 === undefined;
        }
        const v9 = isUndefined;
        const email2: string = `<${v9}`;
        const email: number = isUndefined2 + email2;
        return `>${email}`;
    }

    static testHttpStatus(param_0, param_1, param_2): void     {
        let toString = v9.toString;
        v9.toString(200);
        toString = v9.toString(v10);
        toString = v9.toString;
        v10 = 404;
        v9.toString(v10);
        toString = v9.toString(v10);
        toString = v9.toString;
        v10 = 500;
        v9.toString(v10);
        toString = v9.toString(v10);
        toString = v9.toString;
        v10 = 403;
        v9.toString(v10);
        toString = v9.toString(v10);
        v10 = `,${toString3 + `,${toString2}`}`;
        const v9: number = toString4 + v10;
        toString = `,${v9}`;
        return toString5 + toString;
    }

    static testArrayBuffer(param_0, param_1, param_2): void     {
        let v9 = ArrayBuffer;
        let v10 = 8;
        let arrayBuffer = new ArrayBuffer();
        v9 = DataView;
        v10 = arrayBuffer;
        arrayBuffer = new arrayBuffer(v9, v10, v11, v12);
        v10 = arrayBuffer2;
        let uint8 = v10.setUint8;
        let v11 = 0;
        let v12 = 72;
        v10.setUint8(v11, v12);
        v10 = arrayBuffer2;
        uint8 = v10.setUint8;
        v11 = 1;
        v12 = 105;
        v10.setUint8(v11, v12);
        v10 = arrayBuffer2;
        uint8 = v10.setUint8;
        v11 = 2;
        v12 = 33;
        v10.setUint8(v11, v12);
        v10 = arrayBuffer2;
        uint8 = v10.getUint8;
        v11 = 0;
        v10.getUint8(v11);
        uint8 = v10.getUint8(v11);
        v10 = arrayBuffer2;
        uint8 = v10.getUint8;
        v11 = 1;
        v10.getUint8(v11);
        uint8 = v10.getUint8(v11);
        v10 = arrayBuffer2;
        uint8 = v10.getUint8;
        v11 = 2;
        v10.getUint8(v11);
        uint8 = v10.getUint8(v11);
        let v13 = String;
        v13 = v13(uint82);
        v11 = `,${v13}`;
        v13 = String;
        v13 = uint83;
        v10 = v13(v13) + v11;
        uint8 = `,${v10}`;
        v10 = String;
        v11 = uint84;
        return v10(v11) + uint8;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
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

    static testSetOperations(param_0, param_1, param_2): void     {
        const v9 = Set;
        let set = new Set(v9);
        let v10 = set;
        let add = v10.add;
        v10.add(1);
        v10 = set;
        add = v10.add;
        v11 = 2;
        v10.add(v11);
        v10 = set;
        add = v10.add;
        v11 = 3;
        v10.add(v11);
        v10 = set;
        add = v10.add;
        v11 = 4;
        v10.add(v11);
        add = Set;
        set = new Set(add, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28);
        v10 = set2;
        add = v10.add;
        v11 = 3;
        v10.add(v11);
        v10 = set2;
        add = v10.add;
        v11 = 4;
        v10.add(v11);
        v10 = set2;
        add = v10.add;
        v11 = 5;
        v10.add(v11);
        v10 = set2;
        add = v10.add;
        v11 = 6;
        v10.add(v11);
        add = [];
        v10 = Array;
        v11 = set;
        v10.from(v11);
        from = v10.from(v11);
        v10 = 0;
        let v11 = v10;
        const v12 = from2;
        while (!(v12.length < v11)) {
            loop_0: do {
    let v11: boolean = v12.length < v11;
    let v12 = from2;
} while (v12.length < v11);
        }
        let from: boolean = v12.length < v11;
        let v10 = 0;
        do {
            const v12 = set2;
            const has = v12.has;
            let v13 = from2;
            const v14 = v10;
            v13 = v13[v14];
            v12.has(v13);
            const floor = Boolean(v12.has(v13));
        } while (Boolean(v12.has(v13)));
        const v12 = {  };
        let push = v12.push;
        let v13 = from2;
        const v14 = v10;
        v13 = v13[v14];
        v12.push(v13);
        push = v10;
        const number: number = Number(push);
        let v10: number = _number + 1;
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
        let v9 = {  };
        const v3 = v9;
        v9 = {  };
        const v4 = v9;
        v9 = lex_0_3;
        v9 = v9(v3);
        v9 = lex_0_3;
        v10 = v4;
        v9 = v9(v10);
        v9 = lex_0_3;
        v10 = undefined;
        v9 = v9(v10);
        v10 = v92 + `|${v9}`;
        v9 = `|${v10}`;
        return v93 + v9;
    }

    static static_initializer(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.OK = 200;
        v4.NOT_FOUND = 404;
        v4.ERROR = 500;
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