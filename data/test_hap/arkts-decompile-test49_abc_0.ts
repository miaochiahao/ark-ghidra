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
            let v12 = "DecompileTest49";
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
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static classify(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        if (v6 < 0) {
            const v4 = "positive";
        } else {
            const v6: boolean = v6 < 0;
            const v4 = "negative";
        }
        return "negative";
    }

    static testHash(param_0, param_1, param_2): void     {
        let v7 = lex_0_1;
        v7 = v7("hello");
        v7 = lex_0_1;
        v8 = "world";
        v7 = v7(v8);
        v7 = lex_0_1;
        v8 = "hello";
        v7 = v7(v8);
        let v9 = String;
        let v10 = v7;
        v10 = v73 === v10;
        v9 = v9(v10);
        v7 = `,${v9}`;
        v9 = String;
        v9 = v7;
        v9 = v72 !== v9;
        return v9(v9) + v7;
    }

    static testSwap(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = lex_0_3;
        v6 = v6(v3);
        let v10 = String;
        let v11: Array<unknown> = v6;
        v11 = v11[v12];
        v10 = v10(v11);
        let v8: string = `,${v10}`;
        v10 = String;
        v10 = v6;
        v11 = 4;
        v10 = v10[v11];
        v7 = v10(v10) + v8;
        v6 = `,${v7}`;
        v7 = String;
        v8 = v6;
        v10 = 2;
        v8 = v8[v10];
        return v7(v8) + v6;
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
        v10 = anonymous_method;
        lex_0_0 = v10;
        v10 = func_main_0;
        lex_0_1 = v10;
        v10 = onCreate;
        lex_0_3 = v10;
        let v12 = ViewPU;
        let v12 = Reflect;
        let set = v12.set;
        v12.set(ViewPU.prototype, "finalizeConstruction", func_main_0);
        prototype = ViewPU;
        v12 = undefined / set;
        prototype = set.prototype;
        v15 = "message";
        let v16 = undefined;
        false[v17] = "get".v16;
        v15 = "message";
        v16 = undefined;
        v17 = swapFirstLast;
        false[v16] = "get".v17;
        prototype2.initialRender = testHasOverlap;
        prototype2.rerender = initialRender;
        set.getEntryName = setInitiallyProvidedValue;
        set = set;
        set = set;
        set = registerNamedRoute;
        v12 = func_41;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static hasOverlap(param_0, param_1, param_2, param_3, param_4): void     {
        const v7 = 0;
        do {
            const v9 = 0;
        } while (0);
        const number: number = Number(v10);
        const v9: number = _number + 1;
        for (let v9 = 0; !(v11.length < v10); v10) {
            const v10 = v9;
            let v11 = v4;
            let v11 = v3;
            let v12 = v7;
            const _number = v11[v12];
            v11 = v4;
            v12 = v9;
            return true;
        }
        const number: number = Number(v8);
        const v7: number = _number2 + 1;
    }

    static simpleHash(param_0, param_1, param_2, param_3): void     {
        v3 = v15;
        const v4 = 0;
        const v7 = 0;
        v3 = v15;
        const v4 = 0;
        const v7 = 0;
        let charCodeAt = v10.charCodeAt;
        v10.charCodeAt(v7);
        charCodeAt = v10.charCodeAt(v11);
        v11 = v4;
        v10 = 5 << v11;
        charCodeAt = v4 - v10;
        const v4: number = charCodeAt2 + charCodeAt;
        charCodeAt = v4;
        v4 &= charCodeAt;
        const charCodeAt2 = v7;
        const number: number = Number(charCodeAt2);
        const v7: number = _number + 1;
        for (v0 = v12; !(v9.length < v8); v10) {
            const v8 = v7;
            const v9 = v3;
        }
        const v6 = _number;
        if (!(v6 > 0)) {
            return v6 > 0;
        }
        const v6 = v4;
        const v4: number = -v6;
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

    static testClassify(param_0, param_1, param_2): void     {
        let v7 = lex_0_2;
        v7 = v7(42);
        v7 = lex_0_2;
        v8 = 7;
        v8 = -v8;
        v7 = v7(v8);
        v7 = lex_0_2;
        v8 = 0;
        v7 = v7(v8);
        v8 = v72 + `,${v7}`;
        v7 = `,${v8}`;
        return v73 + v7;
    }

    static testExponent(param_0, param_1, param_2): void     {
        const v7 = 2;
        const v6 = 3;
        const v4 = 1;
        const v10 = 0;
        do {
            let v11 = v4;
            const v4: number = v7 * v11;
            v11 = v10;
            const number: number = Number(v11);
            const v10: number = _number + 1;
        } while (_number);
        do {
            let _number = v3;
            const v3: number = v6 * _number;
            _number = v10;
            const number: number = Number(_number);
            const v10: number = _number + 1;
        } while (_number);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static swapFirstLast(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        let length = v7.length;
        if (!(length > 2)) {
            let length: boolean = length > 2;
            let v7 = 0;
            const v4 = length[v7];
            length = v3;
            v7 = 0;
            const v8 = v3;
            const v11 = v3;
            length = v11.length;
            let v9: number = 1 - length2;
            length[v7] = v8[v9];
            length = v3;
            v9 = v3;
            length = v9.length;
            v7 = 1 - length3;
            length[v7] = v4;
            return v3;
        }
        return v15;
    }

    static testHasOverlap(param_0, param_1, param_2): void     {
        let v9: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v9 = ["/* element_0 */"];
        const v4: string[] = ["/* element_0 */"];
        v9 = ["/* element_0 */", "/* element_1 */"];
        const v5: string[] = ["/* element_0 */", "/* element_1 */"];
        v9 = lex_0_0;
        v9 = v9(v3, v4);
        v9 = lex_0_0;
        v11 = v5;
        v9 = v9(v3, v11);
        v11 = String;
        v11 = v11(v9);
        v9 = `,${v11}`;
        v11 = String;
        return v92(v92) + v9;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testNestedRecord(param_0, param_1, param_2): void     {
        const v5 = {  };
        {  }[v10] = "localhost";
        v9 = v6;
        v10 = "port";
        v9[v10] = "5432";
        v9 = v5;
        v10 = "database";
        v9[v10] = v6;
        v9 = {  };
        v10 = "ttl";
        v9[v10] = "3600";
        v9 = v3;
        v10 = "max";
        v9[v10] = "100";
        v9 = v5;
        v10 = "cache";
        v9[v10] = v3;
        v10 = v5;
        v9 = v10[v11];
        v10 = "host";
        const v7 = v9[v10];
        v10 = v5;
        v11 = "cache";
        v9 = v10[v11];
        v10 = "ttl";
        const v4 = v9[v10];
        v9 = `:${v7}`;
        return v4 + v9;
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