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
            let v12 = "DecompileTest52";
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
        const v5 = v3;
        return v5 > 0;
    }

    static sieve(param_0, param_1, param_2, param_3): void     {
        let v7: Array<unknown> = [];
        const v4: Array<unknown> = [];
        let v8 = 0;
        const v9 = v8;
        while (!(v9 <= v3)) {
            loop_0: do {
    let v9: boolean = v9 <= v3;
} while (v9 <= v3);
        }
        let v7: boolean = v9 <= v3;
        let v8 = 0;
        v7[v8] = false;
        v7 = v4;
        v8 = 1;
        v7[v8] = false;
        v8 = 2;
        let v10 = v8;
        const v9: number = v10 * v8;
        while (!(v9 <= v3)) {
            const v9 = v4;
            let v10 = v8;
            const v11 = v8;
            v10 = v11 * v8;
            const v11 = v10;
            loop_1: while (!(v11 <= v3)) {
    loop_2: do {
    let v11: boolean = v11 <= v3;
} while (v11 <= v3);
}
            const v9: boolean = v11 <= v3;
            const number: number = Number(v9);
            let v8: number = _number + 1;
            continue;
        }
        let v7 = _number;
        let v8 = 2;
        const _number = v8;
        while (!(_number <= v3)) {
            loop_3: do {
    let _number: boolean = _number <= v3;
} while (_number <= v3);
        }
        return _number <= v16;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 * v9;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v6 + v11;
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

    static testLCP(param_0, param_1, param_2): void     {
        let v6 = lex_0_4;
        let v7: Array<unknown> = [];
        v7 = [];
        v6 = v6(v7);
        v6 = lex_0_4;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v6 = v6(v7);
        v6 = `${v6}|`;
        return v6 + v62;
    }

    static tokenize(param_0, param_1, param_2, param_3): void     {
        let v7: Array<unknown> = [];
        const v5: Array<unknown> = [];
        v7 = 0;
        const v9 = v3;
        let charAt = v9.charAt;
        v9.charAt(v4);
        charAt = v9.charAt(v10);
        charAt = charAt2;
        if (charAt === " ") {
            let charAt = v4;
            let v4: number = charAt + 1;
        } else {
            let charAt: boolean = charAt === " ";
            if (charAt === "+") {
                const v9 = v5;
                let push = v9.push;
                const v11 = "OP";
                const v12 = "+";
                let v4: v4 = new v4(v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22);
                v9.push(v4);
                push = v4;
                v4 = push + 1;
            } else {
                let push: boolean = charAt === "+";
                if (!(push === "-")) {
                    let push: boolean = push === "-";
                    push = charAt2;
                    let push = Number(!(push <= "9"))();
                    const v9 = v4;
                    let v4 = v3;
                    if (!(v9 < v4.length)) {
                        const hasLength: boolean = v9 < v4.length;
                        let push = hasLength.push;
                        const v12 = "NUM";
                        const v13 = push;
                        push = new push(v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55);
                        hasLength.push(push3);
                    }
                    const push3 = v3;
                    let charAt = push3.charAt;
                    const v12 = v4;
                    push3.charAt(v12);
                    charAt = push3.charAt(v12);
                    charAt = charAt3;
                    charAt = charAt3;
                    charAt = push;
                    let push: number = charAt + charAt3;
                    charAt = v4;
                    let v4: number = charAt + 1;
                }
                const charAt3 = v5;
                push = charAt3.push;
                const push3 = "OP";
                const v12 = "-";
                let v4: v4 = new v4(push3, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30);
                charAt3.push(v4);
                push = v4;
                v4 = push + 1;
            }
        }
        return v5;
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
        v2 = v19;
        lex_0_4 = func_main_0;
        v9 = anonymous_method;
        lex_0_0 = v9;
        v9 = func_main_0;
        lex_0_3 = v9;
        v9 = onWindowStageCreate;
        lex_0_2 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", testLCP);
        prototype = undefined;
        let v2: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v11 = set * undefined;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = testTokenizer;
        false[v15] = "get".v16;
        prototype2.initialRender = testChainedOps;
        prototype2.rerender = initialRender;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_43;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static testSieve(param_0, param_1, param_2): void     {
        let v5 = lex_0_0;
        v5 = v5(30);
        let v11 = String;
        v11 = v11(v5.length);
        let v9: string = `${v11}:`;
        v11 = String;
        v11 = v5;
        length = 0;
        v11 = v11[length];
        let v8: number = v9 + v11(v11);
        let v7: string = `${v8},`;
        v8 = String;
        v9 = v5;
        v11 = 5;
        v9 = v9[v11];
        v6 = v7 + v8(v9);
        v5 = `${v6},`;
        v6 = String;
        v7 = v5;
        v8 = 9;
        v7 = v7[v8];
        return v5 + v6(v7);
    }

    static slidingMax(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = 0;
        const v9: number = v8;
        let v11 = v3;
        const length = v11.length;
        const length = v3;
        let v11 = v8;
        const v9 = length[v11];
        v11 = 1;
        const v12 = v11;
        while (!(v12 < v4)) {
            const v13 = v3;
            const v15 = v8;
            const v14: number = v15 + v11;
            const v12 = v13[v14];
            if (v12 > v9) {
                const v12 = v3;
                const v14 = v8;
                const v13: number = v14 + v11;
                const v9 = v12[v13];
                const v12 = v11;
                const number: number = Number(v12);
                let v11: number = _number + 1;
                continue;
            }
        }
        let v11: boolean = v12 > v9;
        v11.push(v9);
        const v9 = v8;
        const number: number = Number(v9);
        const v8: number = _number2 + 1;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
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

    static testTokenizer(param_0, param_1, param_2): void     {
        let v5 = lex_0_2;
        v5 = v5("123 + 456 - 78");
        let v11 = String;
        v11 = v11(v5.length);
        let v9: string = `${v11}:`;
        v11 = v5;
        length = 0;
        v11 = v11[length];
        let v8: number = v9 + v11.value;
        let v7: string = `${v8},`;
        v11 = 1;
        v8 = v5[v11];
        v6 = v7 + v8.type;
        v5 = `${v6},`;
        v8 = 4;
        v6 = v5[v8];
        return v5 + v6.value;
    }

    static testChainedOps(param_0, param_1, param_2): void     {
        const v8: Array<unknown> = [];
        -2[8] = v13;
        3[8] = v14;
        v9 = 4;
        -v9[8] = v3;
        5[8] = v4;
        v9 = 6;
        -v9[8] = v5;
        7[8] = v6;
        v9 = 8;
        -v9[8] = v7;
        9[8] = v8;
        v9 = 10;
        -v9[8] = v9;
        const v3: Array<unknown> = v8;
        v9 = v3;
        v9.filter(func_main_0);
        filter = v9.filter(v10);
        v9 = filter2;
        v10 = anonymous_method;
        v9.map(v10);
        map = v9.map(v10);
        v9 = map2;
        let reduce = v9.reduce;
        v10 = initialRender;
        v9.reduce(v10, 0);
        reduce = v9.reduce(v10, v11);
        v10 = String;
        v11 = filter2;
        v10 = v10(v11.length);
        reduce = `${v10},`;
        v10 = String;
        v10 = reduce2;
        return reduce + v10(v10);
    }

    static testSlidingMax(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        -1[6] = v14;
        v7 = 3;
        -v7[6] = v3;
        5[6] = v4;
        3[6] = v5;
        6[6] = v6;
        7[6] = v7;
        const v3: Array<unknown> = v6;
        v6 = lex_0_3;
        v7 = v3;
        v6 = v6(v7, 3);
        let v10 = String;
        v10 = v10(v6.length);
        v8 = `${v10}:`;
        v10 = String;
        v10 = v6;
        length = 0;
        v10 = v10[length];
        v7 = v8 + v10(v10);
        v6 = `${v7},`;
        v7 = String;
        v8 = v6;
        v10 = 2;
        v8 = v8[v10];
        return v6 + v7(v8);
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

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static longestCommonPrefix(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        let length = v7.length;
        if (!(length === 0)) {
            let length: boolean = length === 0;
            let v7 = 0;
            const v4 = length[v7];
            v7 = 1;
            const v8: number = v7;
            const v9 = v3;
            let v11 = v3;
            const v12 = v7;
            const v10 = v11[v12];
            let indexOf = v10.indexOf;
            v11 = v4;
            v10.indexOf(v11);
            indexOf = v10.indexOf(v11);
            if (!(indexOf2 !== 0)) {
                const indexOf2: boolean = indexOf2 !== 0;
                const number: number = Number(indexOf2);
                let v7: number = _number + 1;
            }
            let indexOf = v4;
            let substring = indexOf.substring;
            const v10 = 0;
            const v12 = v4;
            let length = v12.length;
            length2--;
            indexOf.substring(v10, length2);
            substring = indexOf.substring(v10, length2);
            indexOf = substring2;
            length = indexOf.length;
            if (!(length3 === 0)) {
            }
            return "";
        }
        return "";
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