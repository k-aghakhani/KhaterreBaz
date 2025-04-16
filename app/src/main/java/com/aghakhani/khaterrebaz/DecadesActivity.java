package com.aghakhani.khaterrebaz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class DecadesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decades);

        // Initialize ViewPager and TabLayout
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // Setup fragments for decades
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(DecadeFragment.newInstance("دهه 50", "یادتونه رادیو چه صدایی داشت؟", "هر شب دورش جمع می‌شدیم!"));
        fragments.add(DecadeFragment.newInstance("دهه 60", "فیلمای تختی تو تلویزیون!", "همه منتظر بودیم ببینیم چی می‌شه."));
        fragments.add(DecadeFragment.newInstance("دهه 70", "کارتون فوتبالیست‌ها!", "سوباسا قهرمان هممون بود!"));

        // Setup adapter
        DecadePagerAdapter adapter = new DecadePagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("دهه 50");
                    break;
                case 1:
                    tab.setText("دهه 60");
                    break;
                case 2:
                    tab.setText("دهه 70");
                    break;
            }
        }).attach();
    }
}