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
            let v12 = "DecompileTest46";
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

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        v6.push(v11);
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

    static getPort(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        {
            const v7 = v3;
            let db = v7.db;
        }
        super(v97, v98, v99, v100, v101, v102, v103, v104, v105, v106, v107, v108, v109, v110, v111, v112, v113, v114, v115, v116, v117, v118, v119, v120, v121, v122, v123, v124, v125, v126, v127, v128, v129, v130, v131, v132, v133, v134, v135, v136, v137, v138, v139, v140, v141, v142, v143, v144, v145, v146, v147, v148, v149, v150, v151, v152, v153, v154, v155, v156, v157, v158, v159, v160, v161, v162, v163, v164, v165, v166, v167, v168, v169, v170, v171, v172, v173, v174, v175, v176, v177, v178, v179, v180, v181, v182, v183, v184, v185, v186, v187, v188, v189, v190, v191, v192);
        db = db.db;
        return db2.port;
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
        v14 = anonymous_method;
        lex_0_1 = v14;
        const v16 = ViewPU;
        const v16 = Reflect;
        let set = v16.set;
        v16.set(ViewPU.prototype, "finalizeConstruction", message);
        prototype = ViewPU;
        let isUndefined: boolean = set > undefined;
        prototype = set.prototype;
        v19 = "message";
        let v20 = undefined;
        false[v21] = "get".v20;
        v19 = "message";
        v20 = undefined;
        v21 = getEntryName;
        false[v20] = "get".v21;
        prototype2.initialRender = testSetFromArray;
        prototype2.rerender = testObjectEntries;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        isUndefined = func_41;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static arraysEqual(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = v15;
        return false;
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

    static testBooleanOps(param_0, param_1, param_2): void     {
        let v10 = Boolean;
        v10 = v10(1);
        v10 = Boolean;
        v11 = 0;
        v10 = v10(v11);
        v10 = Boolean;
        v11 = "";
        v10 = v10(v11);
        v10 = Boolean;
        v11 = "hello";
        v10 = v10(v11);
        v10 = Boolean;
        v11 = null;
        v10 = v10(v11);
        v10 = Boolean;
        v11 = undefined;
        v10 = v10(v11);
        let v20 = String;
        v20 = v20(v10);
        let v18: string = `,${v20}`;
        v20 = String;
        let v17: number = v20(v20) + v18;
        let v16: string = `,${v17}`;
        v17 = String;
        let v15: number = v17(v18) + v16;
        let v14: string = `,${v15}`;
        v15 = String;
        v16 = v104;
        let v13: number = v15(v16) + v14;
        let v12: string = `,${v13}`;
        v13 = String;
        v14 = v105;
        v11 = v13(v14) + v12;
        v10 = `,${v11}`;
        v11 = String;
        v12 = v106;
        return v11(v12) + v10;
    }

    static testArraysEqual(param_0, param_1, param_2): void     {
        let v11: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v11 = ["/* element_0 */"];
        const v4: string[] = ["/* element_0 */"];
        v11 = ["/* element_0 */", "/* element_1 */"];
        const v5: string[] = ["/* element_0 */", "/* element_1 */"];
        v11 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        const v6: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v11 = lex_0_0;
        v11 = v11(v3, v4);
        v11 = lex_0_0;
        v12 = v3;
        v13 = v5;
        v11 = v11(v12, v13);
        v11 = lex_0_0;
        v12 = v3;
        v13 = v6;
        v11 = v11(v12, v13);
        let v15 = String;
        v15 = v15(v11);
        v13 = `,${v15}`;
        v15 = String;
        v12 = v112(v112) + v13;
        v11 = `,${v12}`;
        v12 = String;
        return v12(v113) + v11;
    }

    static testMultiKeyMap(param_0, param_1, param_2): void     {
        let v10 = Map;
        const map = new Map(v10);
        v10 = [];
        let v11: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][10] = v18;
        v11 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][10] = v19;
        v11 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"][10] = v20;
        v11 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */"][10] = v3;
        v11 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */"][10] = v4;
        const v7: Array<unknown> = v10;
        v11 = 0;
        let v12: number = v11;
        const v13: Array<unknown> = v7;
        let v16 = v7;
        const v17 = v11;
        let v15 = v16[v17];
        v16 = 0;
        let v14 = v15[v16];
        const v13: string = `-${v14}`;
        v15 = v7;
        v16 = v11;
        v14 = v15[v16];
        v15 = 1;
        let v12: number = v14[v15] + v13;
        v14 = map;
        v16 = 1;
        v14.set(v12, v16);
        const number: number = Number(v12);
        let v11: number = _number + 1;
    }

    static testTypedArrays(param_0, param_1, param_2): void     {
        let v9 = 4;
        v8 = new Int32Array();
        v9 = 0;
        v8[v9] = 10;
        v8 = int32Array;
        v9 = 1;
        v8[v9] = 20;
        v8 = int32Array;
        v9 = 2;
        v8[v9] = 30;
        v8 = int32Array;
        v9 = 3;
        v8[v9] = 40;
        const v6 = 0;
        v9 = 0;
        do {
            let v10 = v6;
            const v11 = int32Array;
            const v12 = v9;
            const v6: number = v11[v12] + v10;
            v10 = v9;
            const number: number = Number(v10);
            let v9: number = _number + 1;
        } while (_number);
        do {
            let _number = v4;
            const v11 = v3;
            const v12 = v9;
            const v4: number = v11[v12] + _number;
            _number = v9;
            const number: number = Number(_number);
            let v9: number = _number + 1;
        } while (_number);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testSetFromArray(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = Set;
        let v7: Array<unknown> = v3;
        const v4 = new [](v6, v7);
        v6 = [];
        v6 = [];
        lex_0_0 = v6;
        let forEach = v7.forEach;
        v4.forEach(onForeground);
        v8 = String;
        v8 = v8(v4.size);
        forEach = `,${v8}`;
        v8 = String;
        const length = v8.length;
        return v8(length) + forEach;
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

    static testCompoundShift(param_0, param_1, param_2): void     {
        let v4 = 240;
        v4 = 2 >> v4;
        v7 = 15;
        v5 = 4 << v7;
        v7 = v4;
        const v3: number = v5 | v7;
        let v11 = String;
        v11 = v11(v4);
        let v9: string = `,${v11}`;
        v11 = String;
        v7 = `,${v5(v5) + v9}`;
        v8 = String;
        return v8(v3) + v7;
    }

    static testObjectEntries(param_0, param_1, param_2): void     {
        {  }[v8] = 1;
        v7 = v3;
        v8 = "beta";
        v7[v8] = 2;
        v7 = v3;
        v8 = "gamma";
        v7[v8] = 3;
        v8 = Object;
        v8.keys(v3);
        keys = v8.keys(v9);
        const v5 = "";
        v8 = 0;
        v9 = v8;
        const v10 = keys2;
        let v11 = keys2;
        let v12 = v8;
        let v9 = v11[v12];
        v12 = v9;
        const v10 = v11[v12];
        const length = v12.length;
        if (!(length < 0)) {
            let v13: boolean = length < 0;
            let v12: number = v9 + v13;
            const length: string = `=${v12}`;
            v12 = String;
            v13 = v10;
            const v5: number = v12(v13) + length;
            let v9 = v8;
            const number: number = Number(v9);
            let v8: number = _number + 1;
        }
        const length = v5;
        const v5: string = `;${length}`;
    }

    static testNestedOptional(param_0, param_1, param_2): void     {
        {  }.db = {  };
        const v3 = v11;
        v11 = {  };
        v12 = {  };
        v11.db = v12;
        const v4 = v11;
        const v5 = {  };
        v11 = lex_0_1;
        v12 = v3;
        v11 = v11(v12);
        v11 = lex_0_1;
        v12 = v4;
        v11 = v11(v12);
        v11 = lex_0_1;
        v12 = v5;
        v11 = v11(v12);
        v11 = lex_0_1;
        v12 = undefined;
        v11 = v11(v12);
        let v17 = String;
        v17 = v17(v11);
        let v15: string = `,${v17}`;
        v17 = String;
        let v14: number = v17(v17) + v15;
        let v13: string = `,${v14}`;
        v14 = String;
        v12 = v14(v113) + v13;
        v11 = `,${v12}`;
        v12 = String;
        v13 = v114;
        return v12(v13) + v11;
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