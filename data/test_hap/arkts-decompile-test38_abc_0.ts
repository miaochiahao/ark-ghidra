export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw v6;
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
        const v10 = "testTag";
        const v11 = "Failed to set colorMode. Cause: %{public}s";
        const v13 = JSON;
        v13.stringify(v6);
        stringify = v13.stringify(v14);
        v8.error(v9, v10, v11);
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
        throw v6;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
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
            let v12 = "DecompileTest38";
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
        throw isUndefined === "function";
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return lex_0_0 + v5;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = String;
        let v6 = v3;
        v6 = 2 * v6;
        return v5(v6);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return "error";
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

    static fizzBuzz(param_0, param_1, param_2, param_3): void     {
        const v7 = 1;
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
        lex_0_2 = func_main_0;
        v11 = anonymous_method;
        lex_0_1 = v11;
        v11 = func_main_0;
        lex_0_0 = v11;
    }

    static makeAdder(param_0, param_1, param_2, param_3): void     {
        lex_0_0 = v8;
        return func_main_0;
    }

    static isLeapYear(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const v5: number = 4 % v6;
        if (!(v5 !== 0)) {
            const v6: boolean = v5 !== 0;
            const v5: number = 100 % v6;
            if (!(v5 !== 0)) {
                const v6: boolean = v5 !== 0;
                const v5: number = 400 % v6;
                return true;
            }
            return true;
        }
        return false;
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

    static testFizzBuzz(param_0, param_1, param_2): void     {
        const v4 = lex_0_2;
        const v5 = 15;
        return v4(v5);
    }

    static testLeapYear(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8(2000);
        v8 = lex_0_1;
        v9 = 1900;
        v8 = v8(v9);
        v8 = lex_0_1;
        v9 = 2024;
        v8 = v8(v9);
        v8 = lex_0_1;
        v9 = 2023;
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `,${v14}`;
        v14 = String;
        let v11: number = v14(v14) + v12;
        let v10: string = `,${v11}`;
        v11 = String;
        v9 = v11(v83) + v10;
        v8 = `,${v9}`;
        v9 = String;
        v10 = v84;
        return v9(v10) + v8;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testClosureChain(param_0, param_1, param_2): void     {
        let v8 = lex_0_0;
        v8 = v8(5);
        v8 = lex_0_0;
        v9 = 10;
        v8 = v8(v9);
        v8 = v8;
        v9 = 3;
        v8 = v8(v9);
        v8 = v82;
        v9 = 7;
        v8 = v8(v9);
        let v10 = String;
        v10 = v10(v83);
        v8 = `,${v10}`;
        v10 = String;
        return v84(v84) + v8;
    }

    static testComputedKeys(param_0, param_1, param_2): void     {
        const v4: Array<unknown> = [];
        const v7 = 0;
        do {
            let v9 = v4;
            const v10 = v7;
            const v8 = v9[v10];
            v9 = v8;
        } while (v9 === "name");
    }

    static testMapIteration(param_0, param_1, param_2): void     {
        const v7 = Map;
        const map = new Map(v7);
        let v8 = map;
        v8.set("x", 10);
        v8 = map;
        set = v8.set;
        v9 = "y";
        v10 = 20;
        v8.set(v9, v10);
        v8 = map;
        set = v8.set;
        v9 = "z";
        v10 = 30;
        v8.set(v9, v10);
        set = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        const v3: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        v8 = 0;
        do {
            const v11 = map;
            let get = v11.get;
            let v12 = v3;
            const v13 = v8;
            v12 = v12[v13];
            v11.get(v12);
            get = v11.get(v12);
            get = get2;
        } while (get !== undefined);
    }

    static testPromiseChain(param_0, param_1, param_2): void     {
        const v7 = Promise;
        let v8 = anonymous_method;
        const anonymous_method: anonymous_method = new anonymous_method(v7, v8);
        v8 = anonymous_method;
        v8.then(onBackground);
        then = v8.then(v9);
        v8 = then2;
        v9 = onBackup;
        v8.catch(v9);
        catch = v8.catch(v9);
        return "created";
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

    static testCharCodeRange(param_0, param_1, param_2): void     {
        const v5 = "";
        const v8 = 65;
        do {
            let v9 = v5;
            const v11 = String;
            const fromCharCode = v11.fromCharCode;
            const v12 = v8;
            v11.fromCharCode(v12);
            const v5: number = v11.fromCharCode(v12) + v9;
            v9 = v8;
            const number: number = Number(v9);
            const v8: number = _number + 1;
        } while (_number);
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