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
        v2 = v9;
        v2.capacity = v10;
        v5 = v2;
        v5.cache = new Map(Map, v7, v8);
        v5 = v2;
        v6 = [];
        v5.accessOrder = [];
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static get(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const cache = v8.cache;
        let get = cache.get;
        v8 = v12;
        cache.get(v8);
        get = cache.get(v8);
        get = get2;
        if (!(get !== undefined)) {
            return get !== undefined;
        }
        const cache = v2;
        let v8 = v3;
        cache.updateOrder(v8);
    }

    static put(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = v2;
        let cache = v8.cache;
        v8 = v14;
        cache.has(v8);
        v8 = true;
        cache = v8.cache;
        v8 = v14;
        cache.set(v8, v15);
        cache = v13;
        v8 = v14;
        cache.updateOrder(v8);
        return;
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

    static testBST(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.insert(50);
        v9 = acc;
        insert = v9.insert;
        v10 = 30;
        v9.insert(v10);
        v9 = acc;
        insert = v9.insert;
        v10 = 70;
        v9.insert(v10);
        v9 = acc;
        insert = v9.insert;
        v10 = 20;
        v9.insert(v10);
        v9 = acc;
        insert = v9.insert;
        v10 = 40;
        v9.insert(v10);
        v9 = acc;
        let search = v9.search;
        v10 = 30;
        v9.search(v10);
        search = v9.search(v10);
        v9 = acc;
        search = v9.search;
        v10 = 60;
        v9.search(v10);
        search = v9.search(v10);
        v9 = acc;
        search = v9.search;
        v10 = 50;
        v9.search(v10);
        search = v9.search(v10);
        let v12 = String;
        v12 = v12(search2);
        v10 = `,${v12}`;
        v12 = String;
        v12 = search3;
        v9 = v12(v12) + v10;
        search = `,${v9}`;
        v9 = String;
        v10 = search4;
        return v9(v10) + search;
    }

    static size(param_0, param_1, param_2): void     {
        const v5 = v2;
        const accessOrder = v5.accessOrder;
        return accessOrder.length;
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
        v0 = v18;
        v1 = v19;
        lex_0_3 = func_main_0;
        let v12 = ViewPU;
        let v12 = Reflect;
        let set = v12.set;
        v12.set(ViewPU.prototype, "finalizeConstruction", testBST);
        prototype = undefined;
        let v1: @system.app = @system.app;
        v12 = undefined / set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v0: @system.app = @system.app;
        v12 = undefined / set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v1: @system.app = @system.app;
        v12 = undefined / set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v12 = undefined / set;
        prototype = set.prototype;
        v15 = "message";
        let v16 = undefined;
        false[v17] = "get".v16;
        v15 = "message";
        v16 = undefined;
        v17 = getEntryName;
        false[v16] = "get".v17;
        prototype2.initialRender = testEveryComplex;
        prototype2.rerender = testCaseConversion;
        set.getEntryName = setInitiallyProvidedValue;
        set = set;
        set = set;
        set = registerNamedRoute;
        v12 = func_47;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static insert(param_0, param_1, param_2, param_3): void     {
        v2 = v11;
        v3 = v12;
        let v8 = v3;
        v3 = new v3();
        const root = v8.root;
        if (!(root === null)) {
            const isNull: boolean = root === null;
            const root = isNull.root;
            do {
                const isNull = v3;
                let v8 = root;
            } while (v8.value < isNull);
            let v8 = root;
            const left = v8.left;
            if (!(left === null)) {
                const isNull: boolean = left === null;
                const left = isNull.left;
                return;
            }
            const isNull = left;
            isNull.left = v3;
            return;
        }
        v2.root = v3;
        return;
    }

    static search(param_0, param_1, param_2, param_3): void     {
        const v6 = v2;
        const root = v6.root;
        do {
            const v6 = v3;
            const v7 = root;
        } while (v7.value === v6);
        return true;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static cloneRecord(param_0, param_1, param_2, param_3): void     {
        let v8 = Object;
        v8.keys(v17);
        keys = v8.keys(v9);
        v8 = 0;
        const v9 = v8;
        const v10 = keys2;
        while (!(v10.length < v9)) {
            loop_0: do {
    let v9: boolean = v10.length < v9;
    let v10 = keys2;
} while (v10.length < v9);
        }
        return v10.length < v9;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testLRUCache(param_0, param_1, param_2): void     {
        let v10 = 3;
        v10 = new 3();
        v10.put("a", "value_a");
        v10 = v3;
        put = v10.put;
        v11 = "b";
        v12 = "value_b";
        v10.put(v11, v12);
        v10 = v3;
        put = v10.put;
        v11 = "c";
        v12 = "value_c";
        v10.put(v11, v12);
        v10 = v3;
        v11 = "a";
        v10.get(v11);
        get = v10.get(v11);
        v10 = v3;
        put = v10.put;
        v11 = "d";
        v12 = "value_d";
        v10.put(v11, v12);
        v10 = v3;
        get = v10.get;
        v11 = "b";
        v10.get(v11);
        get = v10.get(v11);
        v10 = v3;
        get = v10.get;
        v11 = "c";
        v10.get(v11);
        get = v10.get(v11);
        v10 = v3;
        let size = v10.size;
        v10.size();
        size = v10.size();
        let v15 = String;
        v15 = v15(get2);
        let v13: string = `,${v15}`;
        v15 = String;
        v15 = get3;
        v12 = v15(v15) + v13;
        v11 = `,${v12}`;
        v12 = String;
        v13 = get4;
        v10 = v12(v13) + v11;
        size = `,${v10}`;
        v10 = String;
        v11 = size2;
        return v10(v11) + size;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testDeepClone(param_0, param_1, param_2): void     {
        {  }[v7] = 10;
        v6 = v4;
        v7 = "y";
        v6[v7] = 20;
        v6 = v4;
        v7 = "z";
        v6[v7] = 30;
        v6 = lex_0_3;
        v7 = v4;
        v6 = v6(v7);
        v6 = v6;
        v7 = "x";
        v6[v7] = 99;
        let v8 = String;
        let v9 = v4;
        v9 = v9[v10];
        v8 = v8(v9);
        v6 = `,${v8}`;
        v8 = String;
        v8 = v6;
        v9 = "x";
        v8 = v8[v9];
        return v8(v8) + v6;
    }

    static updateOrder(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        let accessOrder = v8.accessOrder;
        let indexOf = accessOrder.indexOf;
        v8 = v13;
        accessOrder.indexOf(v8);
        indexOf = accessOrder.indexOf(v8);
        indexOf = indexOf2;
        if (!(indexOf <= 0)) {
            let v8: boolean = indexOf <= 0;
            accessOrder = v8.accessOrder;
            const push = accessOrder.push;
            v8 = v3;
            accessOrder.push(v8);
            return;
        }
        let v8 = v2;
        accessOrder = v8.accessOrder;
        v8 = indexOf2;
        accessOrder.splice(v8, 1);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testEveryComplex(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v16;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v17;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v18;
        const v3: Array<unknown> = v6;
        v6 = [];
        v7 = 0;
        const v8: number = v7;
        const v9: Array<unknown> = v3;
        const v10 = v3;
        let v11 = v7;
        const v9 = v10[v11];
        const v8 = true;
        v11 = 0;
        const v12 = v11;
        const v13 = v9;
        while (!(v13.length < v12)) {
            const v14 = v9;
            const v15 = v11;
            const v13 = v14[v15];
            const v12: number = 2 % v13;
            if (v12 !== 0) {
                const v8 = false;
                const v12 = v11;
                const number: number = Number(v12);
                let v11: number = _number + 1;
                continue;
            }
        }
        let v11: boolean = v12 !== 0;
        v11.push(v8);
        const v8 = v7;
        const number: number = Number(v8);
        let v7: number = _number2 + 1;
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

    static testParseIntRadix(param_0, param_1, param_2): void     {
        let v7 = parseInt;
        v7 = v7("1010", 2);
        v7 = parseInt;
        v8 = "FF";
        v9 = 16;
        v7 = v7(v8, v9);
        v7 = parseInt;
        v8 = "77";
        v9 = 8;
        v7 = v7(v8, v9);
        let v11 = String;
        v11 = v11(v7);
        v9 = `,${v11}`;
        v11 = String;
        v8 = v72(v72) + v9;
        v7 = `,${v8}`;
        v8 = String;
        return v8(v73) + v7;
    }

    static testCaseConversion(param_0, param_1, param_2): void     {
        let v8 = "HELLO WORLD";
        let toLowerCase = v8.toLowerCase;
        v8.toLowerCase();
        toLowerCase = v8.toLowerCase();
        v8 = "hello world";
        v8.toUpperCase();
        toUpperCase = v8.toUpperCase();
        v8 = "MiXeD CaSe";
        toLowerCase = v8.toLowerCase;
        v8.toLowerCase();
        toLowerCase = v8.toLowerCase();
        v8 = toUpperCase2 + `|${toLowerCase2}`;
        toLowerCase = `|${v8}`;
        v8 = String;
        v9 = toLowerCase3;
        const length = v9.length;
        return v8(length) + toLowerCase;
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