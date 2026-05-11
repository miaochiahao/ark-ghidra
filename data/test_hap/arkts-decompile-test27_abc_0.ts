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
            return;
        }
        const v11 = JSON;
        stringify = v11.stringify(v16);
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
        const v13 = JSON;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = onBackground;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
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

    constructor(param_0, param_1, param_2)     {
        v2 = v7;
        v2.count = 0;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static add(param_0, param_1, param_2, param_3): void     {
        v2 = v10;
        v2.count = v2.count + v11;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static area(param_0, param_1, param_2): void     {
        let v5 = v2;
        let tag = v5.tag;
        if (!(tag === "circle")) {
            let v5: boolean = tag === "circle";
            tag = v5.tag;
        }
        let v5 = 3;
        const v6 = v2;
        let tag: number = v5 * v6.r;
        return tag * v9.r;
    }

    static copy(param_0, param_1, param_2): void     {
        const v6 = v2;
        const x = v6.x;
        const v7 = v2;
        const y = v7.y;
        return new v7.y(v4, x, y, v7);
    }

    static init(param_0, param_1, param_2, param_3, param_4): void     {
        v6.version = v10;
        v6.label = v11;
        return;
    }

    static reset(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.count = 0;
        return v2;
    }

    static value(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.count;
    }

    static build(param_0, param_1, param_2): void     {
        const v5 = "SELECT * FROM ";
        const v6 = v2;
        const v7 = v2;
        const conditions = v7.conditions;
        const length = conditions.length;
        const v7: boolean = length > 0;
        const v9 = v2;
        const conditions = v9.conditions;
        while (!(v7 < conditions2.length)) {
        }
    }

    static limit(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.limitVal = v9;
        return v2;
    }

    static where(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const conditions = v7.conditions;
        return v10;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_5 = onBackground;
        throw acc;
    }

    static getName(param_0, param_1, param_2): void     {
        return v4.label;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static withDefault(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (v6 !== undefined) {
            return v6 !== undefined;
        }
    }

    static makeRect(param_0, param_1, param_2, param_3, param_4): void     {
        const acc: acc = new acc();
        acc.tag = "rect";
        v7 = acc;
        v7.w = v11;
        v7 = acc;
        v7.h = v12;
        return acc;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static increment(param_0, param_1, param_2): void     {
        v2 = v9;
        v2.count = v2.count + 1;
        return v2;
    }

    static translate(param_0, param_1, param_2, param_3, param_4): void     {
        v2 = v11;
        v2.x = v2.x + v12;
        v6 = v2;
        v6.y = v2.y + v13;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static makeCircle(param_0, param_1, param_2, param_3): void     {
        const acc: acc = new acc();
        acc.tag = "circle";
        v6 = acc;
        v6.r = v10;
        return acc;
    }

    static distanceTo(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const x = v8.x;
        v8 = v14;
        const v4: number = x - v8.x;
        v8 = v13;
        const y = v8.y;
        v8 = v14;
        const v5: number = y - v8.y;
        v8 = Math;
        let v10: number = v4;
        const v9: number = v10 * v4;
        v9 += v5 * v5;
        return v8.sqrt(v9);
    }

    static getVersion(param_0, param_1, param_2): void     {
        return v4.version;
    }

    static testStaticInit(param_0, param_1, param_2): void     {
        let init = v5.init;
        const v7 = "myapp";
        name = v7.getName();
        init = `${name2}:`;
        const name2 = String;
        let version = v7.getVersion;
        version = v7.getVersion();
        return init + name2(version);
    }

    static testTaggedUnion(param_0, param_1, param_2): void     {
        makeCircle = v7.makeCircle(5);
        let makeRect = v7.makeRect;
        v8 = 3;
        makeRect = v7.makeRect(v8, 4);
        v8 = String;
        const v10 = makeCircle2;
        let area = v10.area;
        area = v10.area();
        v8 = v8(area);
        makeRect = `${v8},`;
        v8 = String;
        area = makeRect2;
        area = area.area;
        area = area.area();
        return makeRect + v8(area2);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testDefaultValue(param_0, param_1, param_2): void     {
        const v3 = "hello";
        const v4 = undefined;
        let v8 = lex_0_5;
        v8 = v8(v3, "default");
        v8 = lex_0_5;
        v10 = "default";
        v8 = v8(v4, v10);
        v8 = `${v8},`;
        return v8 + v82;
    }

    static testQueryBuilder(param_0, param_1, param_2): void     {
        const v12 = new "users"();
        where = v12.where("age > 18");
        where = where2.where;
        where = "active = true";
        where = where2.where(where);
        limit = where4.limit(10);
        build = limit2.build();
        return build2;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testDefensiveCopy(param_0, param_1, param_2): void     {
        let v8 = 0;
        let v9 = 0;
        v8 = new 0();
        copy = v8.copy();
        v8 = copy2;
        v9 = 3;
        v8 = v4;
        let distanceTo = v8.distanceTo;
        v9 = copy2;
        distanceTo = v8.distanceTo(v9);
        let v11 = String;
        v11 = v11(distanceTo2);
        v9 = `${v11},`;
        v11 = String;
        v8 = v9 + v4(v4.x);
        distanceTo = `${v8},`;
        v8 = String;
        v9 = copy2;
        x = v9.x;
        return distanceTo + v8(x2);
    }

    static testFluentCounter(param_0, param_1, param_2): void     {
        let acc: acc = new acc();
        const v15: acc = acc;
        let increment = v15.increment;
        increment = v15.increment();
        increment = increment2.increment;
        increment = increment2.increment();
        increment = increment4.increment;
        increment = increment4.increment();
        let value = increment6.value;
        value = increment6.value();
        acc = new acc(value, increment6, increment5, increment4, increment3, increment2, increment, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25);
        const increment2 = acc2;
        increment = 10;
        add = increment2.add(increment);
        add = add2.add;
        add = 5;
        add = add2.add(add);
        value = add4.value;
        value = add4.value();
        let add3 = String;
        const add2 = value2;
        add3 = add3(add2);
        value = `${add32},`;
        const add32 = String;
        add3 = value3;
        return value + add32(add3);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        const get2 = v2;
        return;
    }

    static static_initializer(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.version = 0;
        v4.label = "";
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
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