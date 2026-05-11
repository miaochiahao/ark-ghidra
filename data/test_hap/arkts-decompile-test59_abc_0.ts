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
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
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
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
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
        const v4 = v2;
        v4.parts = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 * 3;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 > 20;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 + 5;
    }

    static get(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.value;
    }

    static map(param_0, param_1, param_2, param_3): void     {
        v2 = v10;
        v2.value = v11(v2.value);
        return v2;
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

    static getArea(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        let kind = v6.kind;
        if (!(kind === "circle")) {
            const v6: boolean = kind === "circle";
            kind = v6.kind;
            let kind = v3;
            let v7 = kind;
            const w = v7.w;
            v7 = kind;
            return w * v7.h;
        }
        let kind = v3;
        let v7 = kind;
        const radius = v7.radius;
        v7 = radius * radius;
        return v7 * 3;
    }

    static evaluate(param_0, param_1, param_2, param_3): void     {
        let v5 = 0;
        let v8 = lex_0_0;
        v8 = v8(v17, v5);
        v8 = v8;
        v9 = 0;
        const v4 = v8[v9];
        v8 = v8;
        v9 = 1;
        v5 = v8[v9];
        v9 = v17;
        let v12 = v3;
        let charAt = v12.charAt;
        v12.charAt(v5);
        charAt = v12.charAt(v13);
        charAt = v5;
        const number: number = Number(charAt);
        let v5: number = _number + 1;
        let _number = lex_0_0;
        v12 = v17;
        v13 = v5;
        _number = _number(v12, v13);
        _number = _number2;
        v12 = 0;
        let v9 = _number[v12];
        _number = _number2;
        v12 = 1;
        v5 = _number[v12];
        _number = charAt2;
        if (!(_number === "+")) {
            let _number: boolean = _number === "+";
            if (!(_number === "-")) {
                let _number: boolean = _number === "-";
                if (!(_number === "*")) {
                    let _number: boolean = _number === "*";
                    let _number = v4;
                    const v4: number = _number / v9;
                }
                let _number = v4;
                const v4: number = _number * v9;
            }
            let _number = v4;
            const v4: number = _number - v9;
        }
        let _number = v4;
        const v4: number = _number + v9;
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
        lex_0_1 = func_main_0;
        v10 = anonymous_method;
        lex_0_3 = v10;
        v10 = func_main_0;
        lex_0_4 = v10;
        v10 = onCreate;
        lex_0_0 = v10;
        let v12 = ViewPU;
        let v12 = Reflect;
        let set = v12.set;
        v12.set(ViewPU.prototype, "finalizeConstruction", map);
        prototype = undefined;
        set = @system.app;
        v12 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v12 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        set = @system.app;
        v12 = @system.app;
        prototype = set.prototype;
        v15 = "message";
        let v16 = undefined;
        false[v17] = "get".v16;
        v15 = "message";
        v16 = undefined;
        v17 = Pipeline;
        false[v16] = "get".v17;
        prototype2.initialRender = toString;
        prototype2.rerender = testEvaluate;
        set.getEntryName = getEntryName;
        set = set;
        set = set;
        set = registerNamedRoute;
        v12 = updateStateVars;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static filter(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        const v7 = v2;
        const v6: number = v2 / [];
        v6.value = v12;
        return v10;
    }

    static append(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const parts = v7.parts;
        parts.push(v11);
        return v10;
    }

    static length(param_0, param_1, param_2): void     {
        const v6 = 0;
        const v6 = 0;
        let v10 = v2;
        v10 = v6;
        parts = parts2[v10];
        v7 = v6;
        const number: number = Number(v7);
        const v6: number = _number + 1;
        for (v0 = v11; !(v7 < parts.length); v7) {
            let v7 = v6;
            const v9 = v2;
            let parts = v9.parts;
        }
        return _number;
    }

    static testShapes(param_0, param_1, param_2): void     {
        let v10 = {  };
        const v5 = v10;
        v10 = {  };
        const v8 = v10;
        v10 = lex_0_3;
        v10 = v10(v5);
        v10 = lex_0_3;
        v11 = v8;
        v10 = v10(v11);
        v10 = lex_0_4;
        v11 = v5;
        v10 = v10(v11);
        v10 = lex_0_4;
        v11 = v8;
        v10 = v10(v11);
        let v16 = String;
        v16 = v16(v10);
        let v14: string = `${v16},`;
        v16 = String;
        let v13: number = v14 + v16(v16);
        let v12: string = `${v13},`;
        v13 = String;
        v11 = v12 + v13(v103);
        v10 = `${v11},`;
        v11 = String;
        v12 = v104;
        return v10 + v11(v12);
    }

    static prepend(param_0, param_1, param_2, param_3): void     {
        v3 = v15;
        const v6: Array<unknown> = [];
        v3[6] = v12;
        const v4: Array<unknown> = v6;
        const v7 = 0;
        const v8 = v7;
        const v10 = v2;
        const parts = v10.parts;
        while (!(v8 < parts.length)) {
            loop_0: do {
    let hasLength: boolean = v8 < parts.length;
    let v10 = v2;
    parts = v10.parts;
} while (hasLength < parts.length);
        }
        const hasLength: boolean = hasLength < parts.length;
        hasLength2.parts = v4;
        return v14;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static parseNumber(param_0, param_1, param_2, param_3, param_4): void     {
        const v10 = v4;
        const v11 = v3;
        while (false) {
            acc[3] = v2913;
            let charAt = v11.charAt;
            const v12 = v4;
            v11.charAt(v12);
            charAt = v11.charAt(v12);
            const charAt2 = v4;
            const number: number = Number(charAt2);
            v4 = _number + 1;
            continue;
        }
        const v12 = v3;
        v12.charAt(v18);
        charAt = v12.charAt(v13);
    }

    static toString(param_0, param_1, param_2): void     {
        let v6 = v2;
        const parts = v6.parts;
        v6 = "";
        parts.join(v6);
        return parts.join(v6);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static getPerimeter(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        let kind = v6.kind;
        if (!(kind === "circle")) {
            const v6: boolean = kind === "circle";
            kind = v6.kind;
            let kind = v3;
            const v6 = 2;
            let v8 = kind;
            let w = v8.w;
            v8 = kind;
            return v6 * (w + v8.h);
        }
        let kind = v3;
        let w = kind;
        w = 2 * w.radius;
        return w * 3;
    }

    static testEvaluate(param_0, param_1, param_2): void     {
        let v7 = lex_0_1;
        v7 = v7("3+5");
        v7 = lex_0_1;
        v8 = "10-3*2";
        v7 = v7(v8);
        v7 = lex_0_1;
        v8 = "100/4+10";
        v7 = v7(v8);
        let v11 = String;
        v11 = v11(v7);
        let v9: string = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
    }

    static testPipeline(param_0, param_1, param_2): void     {
        let v6 = 10;
        v6 = new 10();
        let map = v6.map;
        v6.map(onDestroy);
        v7 = onWindowStageDestroy;
        v3.filter(v7, 0);
        map = v3.map;
        v7 = anonymous_method;
        v3.map(v7);
        map = String;
        v7 = v3;
        let get = v7.get;
        v7.get();
        get = v7.get();
        return map(get);
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

    static testStringBuilder(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v8: acc = acc;
        v8.append("Hello");
        v8 = acc;
        append = v8.append;
        v9 = " ";
        v8.append(v9);
        v8 = acc;
        append = v8.append;
        v9 = "World";
        v8.append(v9);
        v8 = acc;
        v9 = ">>> ";
        v8.prepend(v9);
        v8 = acc;
        v8.toString();
        toString = v8.toString();
        v8 = acc;
        let length = v8.length;
        v8.length();
        length = v8.length();
        v8 = toString2;
        length = `${v8}|`;
        v8 = String;
        v9 = length2;
        return length + v8(v9);
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