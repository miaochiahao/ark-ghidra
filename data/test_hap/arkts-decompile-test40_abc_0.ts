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

    constructor(param_0, param_1, param_2, param_3, param_4, param_5, param_6, param_7, param_8)     {
        v2 = v17;
        v6 = v21;
        v7 = v22;
        v8 = v23;
        const v10 = v6;
        if (v10 === undefined) {
            const v10 = 1;
            v6 = -v10;
            const v10 = v7;
            if (v10 === undefined) {
                v7 = undefined;
            }
            const v11 = ObservedPropertySimplePU;
            let v12 = "DecompileTest40";
            const v14 = "message";
            v10.__message = new ObservedPropertySimplePU(v12, v13, v14, v15, v16, v17, v18, v19, v20, v21);
            const initiallyProvidedValue = v11.setInitiallyProvidedValue;
            v12 = v4;
            v11.setInitiallyProvidedValue(v12);
            const finalizeConstruction = v11.finalizeConstruction;
            v11.finalizeConstruction();
        }
        let isUndefined: boolean = v10 === undefined;
        const v11 = v5;
        let v12 = v6;
        const v13 = v8;
        super(isUndefined, v11, v12);
        isUndefined = super(isUndefined, v11, v12);
        v2 = isUndefined;
        isUndefined = typeof v7;
        isUndefined.paramsGenerator_ = v7;
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v7 = v3;
        v7.concat(v13);
        return v7.concat(v8);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const v5: number = 2 % v6;
        return v5 === 0;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v9 * v5;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v11 + v6;
    }

    static fn(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return lex_0_0 * v5;
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

    static getX(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.x;
    }

    static getY(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.y;
    }

    static testFlat(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v15;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v16;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v17;
        const v4: Array<unknown> = v6;
        v6 = [];
        v7 = 0;
        const v8: number = v7;
        const v9: Array<unknown> = v4;
        const v9 = v4;
        let v10 = v7;
        const v8 = v9[v10];
        v10 = 0;
        const v11 = v10;
        const v12 = v8;
        while (!(v12.length < v11)) {
            loop_0: do {
    let v11: boolean = v12.length < v11;
    let v12 = v8;
} while (v12.length < v11);
        }
        const v8: boolean = v12.length < v11;
        const number: number = Number(v8);
        let v7: number = _number + 1;
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
        lex_0_0 = func_main_0;
        const v15 = ViewPU;
        const v15 = Reflect;
        let set = v15.set;
        v15.set(ViewPU.prototype, "finalizeConstruction", getX);
        try {
        } catch (e) {
            throw acc;
        }
        set = set;
        let isUndefined: boolean = set != undefined;
        prototype = set.prototype;
        let v18 = "message";
        let v19 = undefined;
        false[v20] = "get".v19;
        v18 = "message";
        v19 = undefined;
        v20 = testPipeline;
        false[v19] = "get".v20;
        prototype2.initialRender = translate;
        prototype2.rerender = distanceTo;
        set.getEntryName = createMultiplier;
        set = set;
        set = registerNamedRoute;
        isUndefined = testWordFrequency;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static testPoint(param_0, param_1, param_2): void     {
        let v9 = 3;
        let v10 = 4;
        const v4 = new 4();
        v9 = 6;
        v10 = 8;
        const v5 = new 8(v8, v9);
        v10 = v5;
        v4.distanceTo(v10);
        distanceTo = v4.distanceTo(v10);
        let translate = v9.translate;
        v10 = 10;
        v4.translate(v10, 20);
        translate = v4.translate(v10, v11);
        v10 = String;
        v11 = distanceTo2;
        v10 = v10(v11);
        translate = `,${v10}`;
        v10 = translate2;
        v10.toString();
        return v10.toString() + translate;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static toString(param_0, param_1, param_2): void     {
        let v7 = "(";
        const v8 = String;
        const v9 = v2;
        const x = v9.x;
        let v6: number = v8(x) + v7;
        const v5: string = `,${v6}`;
        v6 = String;
        const y = v7.y;
        const v4: number = v6(y) + v5;
        return `)${v4}`;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testPipeline(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v11: Array<unknown> = v3;
        let filter = v11.filter;
        v11.filter(onCreate);
        filter = v11.filter(v12);
        let map = filter2.map;
        filter = onWindowStageCreate;
        filter2.map(filter);
        map = filter2.map(filter);
        let reduce = map2.reduce;
        map = EntryBackupAbility;
        const filter2 = 0;
        map2.reduce(map, filter2);
        reduce = map2.reduce(map, filter2);
        reduce = String;
        const map2 = reduce2;
        return reduce(map2);
    }

    static translate(param_0, param_1, param_2, param_3, param_4): void     {
        const v9 = v2;
        let x = v9.x;
        const v7: number = v3 + x;
        x = v15 + v13.y;
        return new v15 + y(v6, v7, x, y, v10, v11);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testNestedMap(param_0, param_1, param_2): void     {
        let v9 = Map;
        let map = new Map(v9);
        v9 = Map;
        map = new Map(v9, v10, v11, v12);
        let v10 = map2;
        let set = v10.set;
        let v11 = "x";
        let v12 = 10;
        v10.set(v11, v12);
        v10 = map2;
        set = v10.set;
        v11 = "y";
        v12 = 20;
        v10.set(v11, v12);
        set = Map;
        map = new Map(set, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23);
        v10 = map3;
        set = v10.set;
        v11 = "x";
        v12 = 30;
        v10.set(v11, v12);
        v10 = map3;
        set = v10.set;
        v11 = "y";
        v12 = 40;
        v10.set(v11, v12);
        v10 = map;
        set = v10.set;
        v11 = "first";
        v12 = map2;
        v10.set(v11, v12);
        v10 = map;
        set = v10.set;
        v11 = "second";
        v12 = map3;
        v10.set(v11, v12);
        v10 = map;
        let get = v10.get;
        v11 = "second";
        v10.get(v11);
        get = v10.get(v11);
        get = 1;
        const v7: number = -get;
        get = get2;
        let v11 = get2;
        let get = v11.get;
        let v12 = "y";
        v11.get(v12);
        get = v11.get(v12);
        const get3 = get;
        const v7 = get;
        let get = String;
        const get3 = v7;
        return get(get3);
    }

    static distanceTo(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const x = v8.x;
        v8 = v14;
        const v4: number = v8.x - x;
        v8 = v13;
        const y = v8.y;
        v8 = v14;
        const v5: number = v8.y - y;
        v8 = Math;
        let v10: number = v4;
        let v9: number = v4 * v10;
        v9 = v5 * v5 + v9;
        v8.sqrt(v9);
        return v8.sqrt(v9);
    }

    static testHigherOrder(param_0, param_1, param_2): void     {
        let v6 = lex_0_0;
        v6 = v6(3);
        v6 = lex_0_0;
        v7 = 5;
        v6 = v6(v7);
        let v8 = String;
        let v9 = v6;
        v9 = v9(4);
        v8 = v8(v9);
        v6 = `,${v8}`;
        v8 = String;
        v8 = v62;
        v9 = 3;
        v8 = v8(v9);
        return v8(v82) + v6;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static createMultiplier(param_0, param_1, param_2, param_3): void     {
        lex_0_0 = v9;
        return func_main_0;
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

    static testFlattenManual(param_0, param_1, param_2): void     {
        const v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v10;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v11;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v12;
        let reduce = v7.reduce;
        let v8 = onRestore;
        let v9: Array<unknown> = [];
        v9 = [];
        v6.reduce(v8, v9);
        reduce = v6.reduce(v8, v9);
        v8 = String;
        v9 = reduce2;
        v8 = v8(v9.length);
        reduce = `,${v8}`;
        v8 = String;
        v8 = reduce2;
        length = 2;
        v8 = v8[length];
        return v8(v8) + reduce;
    }

    static testLocaleCompare(param_0, param_1, param_2): void     {
        const v3 = "apple";
        const v4 = "banana";
        const v5 = "apple";
        let v11: string = v3;
        let localeCompare = v11.localeCompare;
        v11.localeCompare(v4);
        localeCompare = v11.localeCompare(v12);
        v11 = v3;
        localeCompare = v11.localeCompare;
        v12 = v5;
        v11.localeCompare(v12);
        localeCompare = v11.localeCompare(v12);
        v11 = v4;
        localeCompare = v11.localeCompare;
        v12 = v3;
        v11.localeCompare(v12);
        localeCompare = v11.localeCompare(v12);
        let v14 = String;
        let v15 = localeCompare2;
        v15 = v15 > 0;
        v14 = v14(v15);
        v12 = `,${v14}`;
        v14 = String;
        v14 = localeCompare3;
        v14 = v14 === 0;
        v11 = v14(v14) + v12;
        localeCompare = `,${v11}`;
        v11 = String;
        v12 = localeCompare4;
        v12 = v12 < 0;
        return v11(v12) + localeCompare;
    }

    static testWordFrequency(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        const v6: Array<unknown> = [];
        v8 = Map;
        const map = new Map(v8, v9);
        const v9 = 0;
        const v11: Array<unknown> = v6;
        const v12 = v6;
        let v13 = v9;
        const v11 = v12[v13];
        v13 = map;
        let get = v13.get;
        v13.get(v11);
        get = v13.get(v14);
        get = get2;
        if (get === undefined) {
            let v13 = map;
            let set = v13.set;
            const v14 = v11;
            let v15 = 1;
            v13.set(v14, v15);
        } else {
            const isUndefined: boolean = get === undefined;
            set = isUndefined.set;
            const v14 = v11;
            let v15 = get2;
            v15 = 1 + v15;
            isUndefined.set(v14, v15);
        }
        const get2 = v9;
        const number: number = Number(get2);
        const v9: number = _number + 1;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testNumberConstants(param_0, param_1, param_2): void     {
        let v9 = Number;
        let integer = v9.isInteger;
        v9.isInteger(42);
        integer = v9.isInteger(v10);
        v9 = Number;
        v10 = NaN;
        v9.isNaN(v10);
        naN = v9.isNaN(v10);
        v9 = Number;
        v10 = Infinity;
        v9.isFinite(v10);
        finite = v9.isFinite(v10);
        v9 = Number;
        integer = v9.isInteger;
        v10 = 3.14;
        v9.isInteger(v10);
        integer = v9.isInteger(v10);
        let v14 = String;
        v14 = v14(integer2);
        let v12: string = `,${v14}`;
        v14 = String;
        v14 = naN2;
        v10 = `,${v14(v14) + v12}`;
        v11 = String;
        v12 = finite2;
        v9 = v11(v12) + v10;
        integer = `,${v9}`;
        v9 = String;
        v10 = integer3;
        return v9(v10) + integer;
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